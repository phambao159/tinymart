package controller.manager.supplier;

import dao.manager.employee.EmployeeDAO;
import dao.manager.product.ProductDAO;
import dao.manager.product.ProductSizeDAO;
import dao.manager.supplier.ImportDAO;
import dao.manager.supplier.SupplierDAO;
import java.io.IOException;
import model.manager.employee.Employee;
import model.manager.supplier.ImportDetail;
import model.manager.supplier.Supplier;
import model.manager.supplier.Import; // Import model

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
import javafx.util.converter.LongStringConverter;
import model.manager.product.ProductSummary;

public class AddImportController implements Initializable {

    @FXML private ComboBox<Supplier> cbSupplier;
    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private ComboBox<String> cbStatus; // Cập nhật kiểu String
    @FXML private DatePicker dpReceiptDate;
    @FXML private TableView<ImportDetail> tbDetails;
    @FXML private TableColumn<ImportDetail, String> colProductName;
    @FXML private TableColumn<ImportDetail, String> colSize;
    @FXML private TableColumn<ImportDetail, Long> colQuantity;
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
        
        // Listener tìm kiếm sản phẩm
        txtSearchProduct.textProperty().addListener((obs, oldVal, newVal) -> loadProduct());
    }

    private void loadComboBoxData() {
        // 1. Load Supplier
        cbSupplier.setItems(FXCollections.observableArrayList(supplierDAO.getAllSuppliers()));
        
        // 2. Load Warehouse Staff
        ObservableList<Employee> warehouseStaff = FXCollections.observableArrayList(
                employeeDAO.getData().stream()
                        .filter(e -> "Warehouse".equalsIgnoreCase(e.getRole()))
                        .collect(Collectors.toList())
        );
        cbEmployee.setItems(warehouseStaff);

        // 3. Load Status (Mới thêm)
        cbStatus.setItems(FXCollections.observableArrayList("Pending", "Completed", "Cancelled"));
        cbStatus.setValue("Completed"); // Mặc định

        // Custom hiển thị cho Supplier và Employee ComboBox
        setupComboBoxDisplay();
    }

    private void setupTable() {
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("sizeName"));
        
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        
        colPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice"));
        colPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        colExpireDay.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        setupDateColumn();

        colTotal.setCellFactory(column -> new TableCell<ImportDetail, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else {
                    ImportDetail detail = getTableView().getItems().get(getIndex());
                    double subTotal = detail.getQuantity() * detail.getImportPrice();
                    setText(String.format("%,.2f", subTotal));
                }
            }
        });

        tbDetails.setItems(detailList);
        
        // Double click để sửa/xóa dòng
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
        Supplier selectedSupplier = cbSupplier.getValue();
        Employee selectedEmployee = cbEmployee.getValue();
        String selectedStatus = cbStatus.getValue(); // Lấy giá trị status
        LocalDate receiptDate = dpReceiptDate.getValue();

        if (selectedSupplier == null || selectedEmployee == null || selectedStatus == null || detailList.isEmpty()) {
            showAlert("Validation Error", "Please fill in all fields and add at least one product.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Create this import receipt?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                double totalCost = detailList.stream().mapToDouble(d -> d.getQuantity() * d.getImportPrice()).sum();

                // Tạo đối tượng Import mới khớp với Model đã cập nhật
                Import newImport = new Import();
                newImport.setSupplierID(selectedSupplier.getSupplierID());
                newImport.setEmployeeID(selectedEmployee.getEmployeeID());
                newImport.setReceiptDate(receiptDate.atStartOfDay());
                newImport.setTotalCost(totalCost);
                newImport.setStatus(selectedStatus); // Gán status vào model

                // Lưu vào database (Sử dụng hàm saveImport xử lý Transaction cả Master và Detail)
                boolean success = importDAO.saveImport(newImport, detailList);

                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Import saved successfully!").showAndWait();
                    onCancel(event);
                } else {
                    showAlert("Database Error", "Failed to save data to database.");
                }
            }
        });
    }

    @FXML
    private void onCancel(ActionEvent event) {
        ((Stage) cbSupplier.getScene().getWindow()).close();
    }

    private void calculateTotal() {
        double total = detailList.stream().mapToDouble(d -> d.getQuantity() * d.getImportPrice()).sum();
        lblTotalCost.setText(String.format("%,.2f", total));
    }

    private void addProductToTable(ImportDetail detail) {
        detailList.add(detail);
        calculateTotal();
        tbDetails.refresh();
    }

    // --- CÁC HÀM HỖ TRỢ GIAO DIỆN ---

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

    private void loadProduct() {
        String keyword = txtSearchProduct.getText() == null ? "" : txtSearchProduct.getText().trim();
        productContainer.getChildren().clear();
        List<ProductSummary> products = productDAO.getProductSummaries(keyword, null, null, null);

        for (ProductSummary product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/productcard.fxml"));
                VBox productCard = loader.load();
                ProductCardController cardController = loader.getController();
                cardController.setData(product, this::addProductToTable);
                productContainer.getChildren().add(productCard);
            } catch (IOException e) { e.printStackTrace(); }
        }
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}