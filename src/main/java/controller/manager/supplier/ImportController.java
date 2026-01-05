package controller.manager.supplier;

import dao.manager.employee.EmployeeDAO;
import dao.manager.supplier.ImportDAO;
import dao.manager.supplier.ImportDetailDAO;
import dao.manager.supplier.SupplierDAO;
import model.manager.supplier.Import;
import model.manager.supplier.ImportDetail;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ImportController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Import> tbImport;
    @FXML
    private TableColumn<Import, Integer> colID;
    @FXML
    private TableColumn<Import, Integer> colSupplierID;
    @FXML
    private TableColumn<Import, Integer> colEmployeeID;
    @FXML
    private TableColumn<Import, LocalDateTime> colDate;
    @FXML
    private TableColumn<Import, Double> colTotal;
    @FXML
    private TableColumn<Import, Integer> colProduct;
    @FXML
    private TableColumn<Import, String> colStatus;

    private final ImportDAO importDAO = new ImportDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ImportDetailDAO detailDAO = new ImportDetailDAO();

    private ObservableList<Import> allImports = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadImportData();

        // Tìm kiếm nhanh (Instant Search)
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> filterData(newValue));

        // Double click để xem chi tiết
        tbImport.setRowFactory(tv -> {
            TableRow<Import> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Kiểm tra Double Click và dòng không rỗng
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Import selectedImport = row.getItem();

                    // In log để kiểm tra
                    System.out.println("Double clicked on Import ID: " + selectedImport.getImportID());

                    // Gọi hàm mở Popup đã có logic FXMLLoader
                    openEditImportPopup(selectedImport);
                }
            });
            return row;
        });
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("importID"));

        // 1. Hiển thị Tên nhà cung cấp thay vì ID
        colSupplierID.setCellValueFactory(new PropertyValueFactory<>("supplierID"));
        colSupplierID.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    var s = supplierDAO.getSupplierById(id);
                    setText(s != null ? s.getName() : "Unknown ID: " + id);
                }
            }
        });

        // 2. Hiển thị Tên nhân viên thay vì ID
        colEmployeeID.setCellValueFactory(new PropertyValueFactory<>("employeeID"));
        colEmployeeID.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    var e = employeeDAO.getEmployeeById(id);
                    setText(e != null ? e.getFullName() : "Staff ID: " + id);
                }
            }
        });

        // 3. Hiển thị danh sách sản phẩm (Dùng comma-separated string)
        colProduct.setCellValueFactory(new PropertyValueFactory<>("importID"));
        colProduct.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer importID, boolean empty) {
                super.updateItem(importID, empty);
                if (empty || importID == null) {
                    setText(null);
                } else {
                    List<ImportDetail> details = detailDAO.getDetailsByImportID(importID);
                    String summary = details.stream()
                            .map(ImportDetail::getProductName)
                            .distinct()
                            .collect(Collectors.joining(", "));
                    setText(summary.isEmpty() ? "No products" : summary);
                }
            }
        });

        // 4. Status Column (SỬA ĐỔI CHÍNH Ở ĐÂY)
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toUpperCase());
                }
            }
        });

        // 5. Định dạng ngày tháng
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colDate.setCellValueFactory(new PropertyValueFactory<>("receiptDate"));
        colDate.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(formatter));
            }
        });

        // 6. Định dạng tiền tệ
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%,.2f", value));
            }
        });
    }

    private void loadImportData() {
        allImports.setAll(importDAO.getAllImports());
        tbImport.setItems(allImports);
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tbImport.setItems(allImports);
            return;
        }
        String lowerCaseFilter = keyword.toLowerCase();
        ObservableList<Import> filteredList = allImports.stream()
                .filter(i -> String.valueOf(i.getImportID()).contains(lowerCaseFilter)
                || i.getStatus().toLowerCase().contains(lowerCaseFilter))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tbImport.setItems(filteredList);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/addImport.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Create New Import Voucher");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbImport.getScene().getWindow());
            stage.showAndWait();

            loadImportData(); // Refresh lại bảng sau khi đóng cửa sổ thêm mới
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openImportDetails(Import selected) {
        // Logic mở popup xem chi tiết chi tiết (View Only)
        System.out.println("Opening Details for Import ID: " + selected.getImportID());
    }

    @FXML
    private void onSearch(ActionEvent event) {
        filterData(txtSearch.getText());
    }

    private void openEditImportPopup(Import selected) {
        try {
            // 1. Load file FXML của màn hình Edit
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/editImport.fxml"));
            Parent root = loader.load();

            // 2. Lấy Controller của màn hình Edit
            EditImportController controller = loader.getController();

            // 3. QUAN TRỌNG: Truyền đối tượng Import vừa chọn sang Controller mới
            controller.setData(selected);

            // 4. Hiển thị cửa sổ mới dưới dạng Modal (bắt buộc xử lý xong mới được quay lại)
            Stage stage = new Stage();
            stage.setTitle("Edit Import Receipt: #" + selected.getImportID());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbImport.getScene().getWindow());

            stage.showAndWait();

            // 5. Sau khi đóng cửa sổ Edit, tự động làm mới bảng dữ liệu ở màn hình chính
            loadImportData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
