package controller.manager.supplier;

import dao.manager.employee.EmployeeDAO;
import dao.manager.product.ProductDAO;
import dao.manager.supplier.ImportDAO;
import dao.manager.supplier.ImportDetailDAO;
import dao.manager.supplier.SupplierDAO;
import java.io.IOException;
import model.manager.employee.Employee;
import model.manager.supplier.Import;
import model.manager.supplier.ImportDetail;
import model.manager.supplier.Supplier;
import model.manager.product.ProductSummary;

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

public class EditImportController implements Initializable {

    @FXML
    private ComboBox<Supplier> cbSupplier;
    @FXML
    private ComboBox<Employee> cbEmployee;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private DatePicker dpReceiptDate;
    @FXML
    private TableView<ImportDetail> tbDetails;
    @FXML
    private TableColumn<ImportDetail, String> colProductName;
    @FXML
    private TableColumn<ImportDetail, String> colSize;
    @FXML
    private TableColumn<ImportDetail, Long> colQuantity;
    @FXML
    private TableColumn<ImportDetail, Double> colPrice;
    @FXML
    private TableColumn<ImportDetail, LocalDate> colExpireDay;
    @FXML
    private TableColumn<ImportDetail, Double> colTotal;
    @FXML
    private Label lblTotalCost;
    @FXML
    private TextField txtSearchProduct;
    @FXML
    private FlowPane productContainer;

    private final ObservableList<ImportDetail> detailList = FXCollections.observableArrayList();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ImportDAO importDAO = new ImportDAO();
    private final ImportDetailDAO detailDAO = new ImportDetailDAO();

    private Import currentImport; // Biến lưu trữ phiếu nhập đang sửa

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadComboBoxData();
        loadProduct();
        txtSearchProduct.textProperty().addListener((obs, oldVal, newVal) -> loadProduct());
    }

    // Hàm nhận dữ liệu từ ImportController
    public void setData(Import selectedImport) {
        this.currentImport = selectedImport;

        // 1. Đổ dữ liệu vào ComboBox và DatePicker
        // Tìm đúng đối tượng Supplier trong danh sách của ComboBox dựa trên ID
        cbSupplier.getItems().stream()
                .filter(s -> s.getSupplierID() == selectedImport.getSupplierID())
                .findFirst().ifPresent(s -> cbSupplier.setValue(s));

        cbEmployee.getItems().stream()
                .filter(e -> e.getEmployeeID() == selectedImport.getEmployeeID())
                .findFirst().ifPresent(e -> cbEmployee.setValue(e));

        dpReceiptDate.setValue(selectedImport.getReceiptDate().toLocalDate());
        cbStatus.setValue(selectedImport.getStatus());

        // 2. Load danh sách chi tiết sản phẩm của phiếu nhập này
        List<ImportDetail> details = detailDAO.getDetailsByImportID(selectedImport.getImportID());
        detailList.setAll(details);
        calculateTotal();
    }

    private void loadComboBoxData() {
        cbSupplier.setItems(FXCollections.observableArrayList(supplierDAO.getAllSuppliers()));

        ObservableList<Employee> warehouseStaff = FXCollections.observableArrayList(
                employeeDAO.getData().stream()
                        .filter(e -> "Warehouse".equalsIgnoreCase(e.getRole()))
                        .collect(Collectors.toList())
        );
        cbEmployee.setItems(warehouseStaff);

        cbStatus.setItems(FXCollections.observableArrayList("Pending", "Completed", "Cancelled"));

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

        colTotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    ImportDetail detail = getTableView().getItems().get(getIndex());
                    setText(String.format("%,.2f", detail.getQuantity() * detail.getImportPrice()));
                }
            }
        });

        tbDetails.setItems(detailList);
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

        // Cập nhật thông tin vào đối tượng currentImport
        currentImport.setSupplierID(cbSupplier.getValue().getSupplierID());
        currentImport.setEmployeeID(cbEmployee.getValue().getEmployeeID());
        currentImport.setReceiptDate(dpReceiptDate.getValue().atStartOfDay());
        currentImport.setStatus(cbStatus.getValue());
        currentImport.setTotalCost(detailList.stream().mapToDouble(d -> d.getQuantity() * d.getImportPrice()).sum());

        // Gọi DAO để cập nhật (Hàm updateImport cần được viết trong ImportDAO)
        // Lưu ý: Thường sẽ xóa hết Detail cũ và insert lại Detail mới trong Transaction
        boolean success = importDAO.updateImport(currentImport, detailList);

        if (success) {
            new Alert(Alert.AlertType.INFORMATION, "Import updated successfully!").showAndWait();
            onCancel(event);
        } else {
            showAlert("Error", "Failed to update database.");
        }
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

    private void setupDateColumn() {
        colExpireDay.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("N/A");
                } else {
                    setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(item));
                }
            }
        });
    }

    private void setupComboBoxDisplay() {
        cbSupplier.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Supplier s) {
                return s == null ? "" : s.getName();
            }

            @Override
            public Supplier fromString(String string) {
                return null;
            }
        });
        cbEmployee.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Employee e) {
                return e == null ? "" : e.getFullName();
            }

            @Override
            public Employee fromString(String string) {
                return null;
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
                VBox productCard = loader.load();
                ProductCardController cardController = loader.getController();
                cardController.setData(product, this::addProductToTable);
                productContainer.getChildren().add(productCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openEditPopup(ImportDetail detail) {
        System.out.println("ID Sản phẩm đang mở: " + detail.getProductID());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/editImportDetail.fxml"));
            VBox root = loader.load();
            EditImportDetailController editController = loader.getController();
            editController.setData(detail, detail.getProductID(),
                    updated -> {
                        tbDetails.refresh();
                        calculateTotal();
                    },
                    deleted -> {
                        detailList.remove(deleted);
                        calculateTotal();
                    }
            );
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}
