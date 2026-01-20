package controller.manager.supplier;

import dao.manager.employee.EmployeeDAO;
import dao.manager.product.ProductDAO;
import dao.manager.supplier.ImportDAO;
import dao.manager.supplier.ImportDetailDAO;
import dao.manager.supplier.SupplierDAO;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import model.manager.employee.Employee;
import model.manager.product.ProductSummary;
import model.manager.supplier.Import;
import model.manager.supplier.ImportDetail;
import model.manager.supplier.Supplier;

public class EditImportController implements Initializable {

    @FXML private ComboBox<Supplier> cbSupplier;
    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpReceiptDate;

    @FXML private TableView<ImportDetail> tbDetails;
    @FXML private TableColumn<ImportDetail, String> colProductName;
    @FXML private TableColumn<ImportDetail, String> colSize;
    @FXML private TableColumn<ImportDetail, Long> colQuantity;
    @FXML private TableColumn<ImportDetail, Integer> colShelfQuantity; // Đã cập nhật kiểu dữ liệu
    @FXML private TableColumn<ImportDetail, Double> colPrice;
    @FXML private TableColumn<ImportDetail, LocalDate> colExpireDay;
    @FXML private TableColumn<ImportDetail, Double> colTotal;

    @FXML private Label lblTotalCost;
    @FXML private TextField txtSearchProduct;
    @FXML private FlowPane productContainer;

    private final ObservableList<ImportDetail> detailList = FXCollections.observableArrayList();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ImportDAO importDAO = new ImportDAO();
    private final ImportDetailDAO detailDAO = new ImportDetailDAO();

    private Import currentImport; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadComboBoxData();
        loadProduct();
        txtSearchProduct.textProperty().addListener((obs, oldVal, newVal) -> loadProduct());
    }

    public void setData(Import selectedImport) {
        this.currentImport = selectedImport;

        // 1. Đổ dữ liệu vào ComboBox dựa trên ID
        cbSupplier.getItems().stream()
                .filter(s -> s.getSupplierID() == selectedImport.getSupplierID())
                .findFirst().ifPresent(s -> cbSupplier.setValue(s));

        cbEmployee.getItems().stream()
                .filter(e -> e.getEmployeeID() == selectedImport.getEmployeeID())
                .findFirst().ifPresent(e -> cbEmployee.setValue(e));

        dpReceiptDate.setValue(selectedImport.getReceiptDate().toLocalDate());
        cbStatus.setValue(selectedImport.getStatus());

        // 2. Load chi tiết phiếu nhập từ database
        List<ImportDetail> details = detailDAO.getDetailsByImportID(selectedImport.getImportID());
        detailList.setAll(details);
        calculateTotal();
    }

    private void setupTable() {
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("sizeName"));

        // Cột Tổng số lượng (Editable)
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        colQuantity.setOnEditCommit(e -> {
            ImportDetail d = e.getTableView().getItems().get(e.getTablePosition().getRow());
            d.setQuantity(e.getNewValue());
            calculateTotal();
            tbDetails.refresh();
        });

        // Cột Số lượng lên kệ (Editable + Validation)
        colShelfQuantity.setCellValueFactory(new PropertyValueFactory<>("shelfQuantity"));
        colShelfQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colShelfQuantity.setOnEditCommit(e -> {
            ImportDetail d = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if (e.getNewValue() > d.getQuantity()) {
                showAlert("Validation Error", "Shelf Quantity cannot exceed Total Quantity (" + d.getQuantity() + ")");
                tbDetails.refresh();
            } else {
                d.setShelfQuantity(e.getNewValue());
            }
        });

        colPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice"));
        colPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colPrice.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setImportPrice(e.getNewValue());
            calculateTotal();
            tbDetails.refresh();
        });

        colExpireDay.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        setupDateColumn();

        // Cột Thành tiền
        colTotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ImportDetail detail = (ImportDetail) getTableRow().getItem();
                    setText(String.format("%,.2f", detail.getQuantity() * detail.getImportPrice()));
                }
            }
        });

        tbDetails.setItems(detailList);
        tbDetails.setEditable(true); // Cho phép sửa bảng
        
        tbDetails.setRowFactory(tv -> {
            TableRow<ImportDetail> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openEditPopup(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (cbSupplier.getValue() == null || cbEmployee.getValue() == null || detailList.isEmpty()) {
            showAlert("Error", "Please fill all information!");
            return;
        }

        // Kiểm tra logic ShelfQuantity cho toàn bộ danh sách
        for (ImportDetail d : detailList) {
            if (d.getShelfQuantity() > d.getQuantity()) {
                showAlert("Logical Error", "Product " + d.getProductName() + " has shelf quantity > total quantity.");
                return;
            }
        }

        // Cập nhật thông tin Header
        currentImport.setSupplierID(cbSupplier.getValue().getSupplierID());
        currentImport.setEmployeeID(cbEmployee.getValue().getEmployeeID());
        currentImport.setReceiptDate(dpReceiptDate.getValue().atStartOfDay());
        currentImport.setStatus(cbStatus.getValue());
        currentImport.setTotalCost(detailList.stream().mapToDouble(d -> d.getQuantity() * d.getImportPrice()).sum());

        // Thực thi Update Transaction (Xóa detail cũ, thêm detail mới có ShelfQuantity)
        boolean success = importDAO.updateImport(currentImport, detailList);

        if (success) {
            new Alert(Alert.AlertType.INFORMATION, "Import updated successfully!").showAndWait();
            onCancel(event);
        } else {
            showAlert("Error", "Failed to update database.");
        }
    }

    private void addProductToTable(ImportDetail detail) {
        // Kiểm tra trùng lặp sản phẩm cùng kích cỡ
        for (ImportDetail d : detailList) {
            if (d.getProductSizeID() == detail.getProductSizeID()) {
                d.setQuantity(d.getQuantity() + detail.getQuantity());
                tbDetails.refresh();
                calculateTotal();
                return;
            }
        }
        detail.setShelfQuantity(0); // Mặc định khi thêm mới trong lúc edit là 0
        detailList.add(detail);
        calculateTotal();
        tbDetails.refresh();
    }

    // --- CÁC HÀM HỖ TRỢ HIỂN THỊ ---

    private void loadComboBoxData() {
        cbSupplier.setItems(FXCollections.observableArrayList(supplierDAO.getAllSuppliers()));
        cbEmployee.setItems(FXCollections.observableArrayList(
            employeeDAO.getData().stream().filter(e -> "Warehouse".equalsIgnoreCase(e.getRole())).collect(Collectors.toList())
        ));
        cbStatus.setItems(FXCollections.observableArrayList("Pending", "Completed", "Cancelled"));
        setupComboBoxDisplay();
    }

    private void setupComboBoxDisplay() {
        cbSupplier.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Supplier s) { return s == null ? "" : s.getName(); }
            @Override public Supplier fromString(String string) { return null; }
        });
        cbEmployee.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Employee e) { return e == null ? "" : e.getFullName(); }
            @Override public Employee fromString(String string) { return null; }
        });
    }

    private void setupDateColumn() {
        colExpireDay.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("N/A");
                else {
                    setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(item));
                    if (item.isBefore(LocalDate.now())) setStyle("-fx-text-fill: red;");
                    else setStyle("");
                }
            }
        });
    }

    private void loadProduct() {
        String keyword = txtSearchProduct.getText() == null ? "" : txtSearchProduct.getText().trim();
        productContainer.getChildren().clear();
        List<ProductSummary> products = productDAO.getProductSummaries(keyword, null, null, null);
        for (ProductSummary product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/productcard.fxml"));
                VBox card = loader.load();
                ProductCardController ctrl = loader.getController();
                ctrl.setData(product, this::addProductToTable);
                productContainer.getChildren().add(card);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void calculateTotal() {
        double total = detailList.stream().mapToDouble(d -> d.getQuantity() * d.getImportPrice()).sum();
        lblTotalCost.setText(String.format("%,.2f", total));
    }

    private void openEditPopup(ImportDetail detail) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/editImportDetail.fxml"));
            VBox root = loader.load();
            EditImportDetailController editController = loader.getController();
            editController.setData(detail, detail.getProductID(),
                    updated -> { tbDetails.refresh(); calculateTotal(); },
                    deleted -> { detailList.remove(deleted); calculateTotal(); }
            );
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        ((Stage) cbSupplier.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}