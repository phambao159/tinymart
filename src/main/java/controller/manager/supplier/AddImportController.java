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
import javafx.application.Platform;
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
import javafx.scene.layout.AnchorPane;
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
    private TableColumn<ImportDetail, Long> colQuantity;       // Tổng nhập
    @FXML
    private TableColumn<ImportDetail, Integer> colShelfQuantity; // Số lượng lên kệ
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpReceiptDate.setValue(LocalDate.now());
        setupTable();
        loadComboBoxData();
        loadProduct();

        // Xóa viền đỏ khi người dùng tương tác lại
        cbSupplier.valueProperty().addListener((obs, oldVal, newVal) -> cbSupplier.setStyle(""));
        cbEmployee.valueProperty().addListener((obs, oldVal, newVal) -> cbEmployee.setStyle(""));
        dpReceiptDate.valueProperty().addListener((obs, oldVal, newVal) -> dpReceiptDate.setStyle(""));
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> cbStatus.setStyle(""));

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
        // 1. Xóa tất cả style lỗi cũ trước khi kiểm tra
        resetValidationStyles();

        // 2. Lấy giá trị
        Supplier supplier = cbSupplier.getValue();
        Employee employee = cbEmployee.getValue();
        LocalDate date = dpReceiptDate.getValue();
        String status = cbStatus.getValue();

        // 3. Kiểm tra từng trường (Validation)
        if (supplier == null) {
            showValidationError(cbSupplier, "Please select a Supplier!");
            return;
        }

        if (employee == null) {
            showValidationError(cbEmployee, "Please select a Staff member!");
            return;
        }

        if (date == null) {
            showValidationError(dpReceiptDate, "Please select a Receipt Date!");
            return;
        }

        if (status == null || status.isEmpty()) {
            showValidationError(cbStatus, "Please select a Status!");
            return;
        }

        // 4. Kiểm tra danh sách sản phẩm
        if (detailList.isEmpty()) {
            showAlert("Empty List", "Please select at least one product to import.");
            return;
        }

        // 5. Kiểm tra logic chi tiết từng dòng trong bảng
        for (ImportDetail d : detailList) {
            if (d.getQuantity() <= 0) {
                showAlert("Invalid Quantity", "Product " + d.getProductName() + " must have a quantity > 0.");
                return;
            }
            if (d.getShelfQuantity() > d.getQuantity()) {
                showAlert("Logical Error", "Product " + d.getProductName()
                        + ": Shelf quantity cannot be greater than total quantity.");
                return;
            }
            if (d.getImportPrice() <= 0) {
                showAlert("Invalid Price", "Product " + d.getProductName() + " must have a price > 0.");
                return;
            }
        }

        // 6. Xác nhận và lưu
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Create this import receipt?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                double totalAmount = detailList.stream().mapToDouble(d -> d.getQuantity() * d.getImportPrice()).sum();

                Import imp = new Import();
               
                imp.setSupplierID(supplier.getSupplierID());
                imp.setEmployeeID(employee.getEmployeeID());
                imp.setReceiptDate(date.atStartOfDay());
                imp.setTotalCost(totalAmount);
                imp.setStatus(status);

                if (importDAO.saveImport(imp, detailList)) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Import receipt created successfully!");
                    successAlert.setHeaderText(null);
                    successAlert.showAndWait();

                    onCancel(event);

                    Platform.runLater(() -> {
                        sendNotificationToWarehouse(imp, supplier, employee);
                    });
                } else {
                    showAlert("Database Error", "Failed to save import receipt to database.");
                }
            }
        });
    }

    // --- CÁC HÀM HỖ TRỢ VALIDATION ---
    private void showValidationError(Control field, String message) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 3px;");
        showAlert("Validation Error", message);
        field.requestFocus();
    }

    private void resetValidationStyles() {
        cbSupplier.setStyle("");
        cbEmployee.setStyle("");
        dpReceiptDate.setStyle("");
        cbStatus.setStyle("");
    }

    // Ghi đè lại hàm showAlert để đồng bộ giao diện WARNING
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void sendNotificationToWarehouse(Import imp, Supplier supplier, Employee creator) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/createnoti.fxml"));
            AnchorPane pane = loader.load();

            // Lấy chính xác controller bạn vừa sửa ở trên
            controller.manager.notification.CreateNotiController notiCtrl = loader.getController();

            String title = "New Stock Inbound: " + supplier.getName();

            StringBuilder sb = new StringBuilder();
            sb.append("Dear Warehouse Team,\n\n");
            sb.append("A new import has been confirmed:\n");
            sb.append("- Supplier: ").append(supplier.getName()).append("\n");
            sb.append("- Date: ").append(LocalDate.now()).append("\n\n");
            sb.append("Items List:\n");

            for (ImportDetail d : detailList) {
                sb.append(String.format(" + %s [%s]: %d units\n",
                        d.getProductName(), d.getSizeName(), d.getQuantity()));
            }

            sb.append("\nPlease prepare for stock inspection and shelf replenishment.");

            // Gọi hàm truyền dữ liệu
            notiCtrl.setExternalData(title, sb.toString(), "Warehouse");

            Stage stage = new Stage();
            stage.setTitle("Confirm Notification");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(pane));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void setupDateColumnCell() {
        colExpireDay.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(item));
                    if (item.isBefore(LocalDate.now())) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void loadProduct() {
        String keyword = txtSearchProduct.getText() == null ? "" : txtSearchProduct.getText().trim();
        productContainer.getChildren().clear();
        List<ProductSummary> products = productDAO.getProductSummaries(keyword, null, null, null,"Active");

        for (ProductSummary product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/productcard.fxml"));
                VBox card = loader.load();
                ProductCardController ctrl = loader.getController();
                ctrl.setData(product, this::addProductToTable);
                productContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        ((Stage) cbSupplier.getScene().getWindow()).close();
    }

}
