package controller.manager.supplier;

import dao.manager.employee.EmployeeDAO;
import dao.manager.product.ProductDAO;
import dao.manager.supplier.ImportDAO;
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

public class AddImportController implements Initializable {

    @FXML private ComboBox<Supplier> cbSupplier;
    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpReceiptDate;
    
    @FXML private TableView<ImportDetail> tbDetails;
    @FXML private TableColumn<ImportDetail, String> colProductName;
    @FXML private TableColumn<ImportDetail, String> colSize;
    @FXML private TableColumn<ImportDetail, Long> colQuantity;       // Tổng nhập
    @FXML private TableColumn<ImportDetail, Integer> colShelfQuantity; // Số lượng lên kệ
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpReceiptDate.setValue(LocalDate.now());
        setupTable();
        loadComboBoxData();
        loadProduct();
        
        // Tìm kiếm sản phẩm thời gian thực
        txtSearchProduct.textProperty().addListener((obs, oldVal, newVal) -> loadProduct());
    }

    private void setupTable() {
        // 1. Gán giá trị cho các cột
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("sizeName"));
        
        // 2. Cột Tổng số lượng (Có thể sửa trực tiếp)
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        colQuantity.setOnEditCommit(e -> {
            ImportDetail d = e.getTableView().getItems().get(e.getTablePosition().getRow());
            d.setQuantity(e.getNewValue());
            calculateTotal();
            tbDetails.refresh();
        });

        // 3. CỘT MỚI: Số lượng trên kệ (Có thể sửa trực tiếp)
        colShelfQuantity.setCellValueFactory(new PropertyValueFactory<>("shelfQuantity"));
        colShelfQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colShelfQuantity.setOnEditCommit(e -> {
            ImportDetail detail = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if (e.getNewValue() > detail.getQuantity()) {
                showAlert("Input Error", "Shelf Quantity cannot be greater than Total Quantity (" + detail.getQuantity() + ")");
                tbDetails.refresh(); // Trả lại giá trị cũ trên giao diện
            } else {
                detail.setShelfQuantity(e.getNewValue());
            }
        });

        // 4. Giá nhập
        colPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice"));
        colPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colPrice.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setImportPrice(e.getNewValue());
            calculateTotal();
            tbDetails.refresh();
        });

        // 5. Ngày hết hạn
        colExpireDay.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        setupDateColumnCell();

        // 6. Thành tiền (Tự động tính)
        colTotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ImportDetail detail = (ImportDetail) getTableRow().getItem();
                    double subTotal = detail.getQuantity() * detail.getImportPrice();
                    setText(String.format("%,.2f", subTotal));
                }
            }
        });

        tbDetails.setItems(detailList);
        tbDetails.setEditable(true); // Cho phép sửa bảng
    }

    @FXML
    private void onSave(ActionEvent event) {
        Supplier supplier = cbSupplier.getValue();
        Employee employee = cbEmployee.getValue();
        String status = cbStatus.getValue();
        LocalDate date = dpReceiptDate.getValue();

        if (supplier == null || employee == null || detailList.isEmpty()) {
            showAlert("Validation Error", "Please fill in all information and add products.");
            return;
        }

        // Kiểm tra logic cuối cùng trước khi lưu
        for (ImportDetail d : detailList) {
            if (d.getShelfQuantity() > d.getQuantity()) {
                showAlert("Logical Error", "Product " + d.getProductName() + " has shelf quantity > total quantity.");
                return;
            }
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Create this import receipt?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                double totalAmount = detailList.stream().mapToDouble(d -> d.getQuantity() * d.getImportPrice()).sum();

                Import imp = new Import();
                imp.setSupplierID(supplier.getSupplierID());
                imp.setEmployeeID(employee.getEmployeeID());
                imp.setReceiptDate(date.atStartOfDay());
                imp.setTotalCost(totalAmount);
                imp.setStatus(status);

                // Lưu qua DAO (Xử lý transaction cả Header và Detail có ShelfQuantity)
                if (importDAO.saveImport(imp, detailList)) {
                    new Alert(Alert.AlertType.INFORMATION, "Saved successfully!").showAndWait();
                    onCancel(event);
                } else {
                    showAlert("Database Error", "Failed to save import receipt.");
                }
            }
        });
    }

    private void calculateTotal() {
        double total = detailList.stream().mapToDouble(d -> d.getQuantity() * d.getImportPrice()).sum();
        lblTotalCost.setText(String.format("%,.2f", total));
    }

    private void addProductToTable(ImportDetail detail) {
        // Kiểm tra xem đã có trong bảng chưa
        for (ImportDetail d : detailList) {
            if (d.getProductSizeID() == detail.getProductSizeID()) {
                d.setQuantity(d.getQuantity() + 1);
                tbDetails.refresh();
                calculateTotal();
                return;
            }
        }
        detail.setShelfQuantity(0); // Mặc định số lượng trên kệ là 0 khi mới thêm
        detailList.add(detail);
        calculateTotal();
    }

    private void loadComboBoxData() {
        cbSupplier.setItems(FXCollections.observableArrayList(supplierDAO.getAllSuppliers()));
        cbEmployee.setItems(FXCollections.observableArrayList(
            employeeDAO.getData().stream().filter(e -> "Warehouse".equalsIgnoreCase(e.getRole())).collect(Collectors.toList())
        ));
        cbStatus.setItems(FXCollections.observableArrayList("Pending", "Completed", "Cancelled"));

        cbStatus.setValue("Pending");


        // Set hiển thị tên
        cbSupplier.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Supplier s) { return s == null ? "" : s.getName(); }
            @Override public Supplier fromString(String string) { return null; }
        });
        cbEmployee.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Employee e) { return e == null ? "" : e.getFullName(); }
            @Override public Employee fromString(String string) { return null; }
        });
    }

    private void setupDateColumnCell() {
        colExpireDay.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
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

    @FXML private void onCancel(ActionEvent event) {
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