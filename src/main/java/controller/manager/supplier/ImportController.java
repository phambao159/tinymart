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
import javafx.scene.layout.HBox;
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

    @FXML
    private HBox hSearch;
    @FXML
    private ToggleGroup filterGroup; // Phải trùng với fx:id trong FXML

    private final ImportDAO importDAO = new ImportDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ImportDetailDAO detailDAO = new ImportDetailDAO();

    private ObservableList<Import> allImports = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadImportData();

        // 1. Lắng nghe sự thay đổi của ô tìm kiếm
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // 2. Lắng nghe sự thay đổi của ToggleGroup (RadioButton)
        filterGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // 3. Double click để xem chi tiết
        tbImport.setRowFactory(tv -> {
            TableRow<Import> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openEditImportPopup(row.getItem());
                }
            });
            return row;
        });
    }

    /**
     * Hàm quan trọng nhất: Kết hợp lọc theo từ khóa VÀ lọc theo trạng thái
     */
    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        
        // Lấy text của RadioButton đang chọn (All, Completed, Pending, Canceled)
        RadioButton selectedRadio = (RadioButton) filterGroup.getSelectedToggle();
        String statusFilter = (selectedRadio != null) ? selectedRadio.getText() : "All";

        ObservableList<Import> filteredList = allImports.stream()
                .filter(item -> {
                    // Lọc theo từ khóa (ID hoặc Trạng thái)
                    boolean matchesSearch = keyword.isEmpty() 
                            || String.valueOf(item.getImportID()).contains(keyword)
                            || item.getStatus().toLowerCase().contains(keyword);

                    // Lọc theo trạng thái RadioButton
                    boolean matchesStatus = statusFilter.equals("All") 
                            || item.getStatus().equalsIgnoreCase(statusFilter);

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tbImport.setItems(filteredList);
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("importID"));

        // Hiển thị Tên nhà cung cấp
        colSupplierID.setCellValueFactory(new PropertyValueFactory<>("supplierID"));
        colSupplierID.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) setText(null);
                else {
                    var s = supplierDAO.getSupplierById(id);
                    setText(s != null ? s.getName() : "Unknown ID: " + id);
                }
            }
        });

        // Hiển thị Tên nhân viên
        colEmployeeID.setCellValueFactory(new PropertyValueFactory<>("employeeID"));
        colEmployeeID.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) setText(null);
                else {
                    var e = employeeDAO.getEmployeeById(id);
                    setText(e != null ? e.getFullName() : "Staff ID: " + id);
                }
            }
        });

        // Hiển thị danh sách sản phẩm
        colProduct.setCellValueFactory(new PropertyValueFactory<>("importID"));
        colProduct.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer importID, boolean empty) {
                super.updateItem(importID, empty);
                if (empty || importID == null) setText(null);
                else {
                    List<ImportDetail> details = detailDAO.getDetailsByImportID(importID);
                    String summary = details.stream()
                            .map(ImportDetail::getProductName)
                            .distinct()
                            .collect(Collectors.joining(", "));
                    setText(summary.isEmpty() ? "No products" : summary);
                }
            }
        });

        // Định dạng Badge cho Status
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(status.toUpperCase());
                    String baseStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 10; -fx-font-size: 11px;";
                    
                    if (status.equalsIgnoreCase("Completed")) {
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #1E8449;");
                    } else if (status.equalsIgnoreCase("Pending")) {
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #FF9800;");
                    } else if (status.equalsIgnoreCase("Canceled")) {
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #E74C3C;");
                    } else {
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #BDC3C7;");
                    }
                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });

        // Định dạng ngày
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colDate.setCellValueFactory(new PropertyValueFactory<>("receiptDate"));
        colDate.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(formatter));
            }
        });

        // Định dạng tiền tệ
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
        applyFilters(); // Đảm bảo khi load dữ liệu vẫn tuân thủ bộ lọc đang chọn
    }

    @FXML
    private void onSearch(ActionEvent event) {
        applyFilters();
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
            loadImportData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditImportPopup(Import selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/editImport.fxml"));
            Parent root = loader.load();
            EditImportController controller = loader.getController();
            controller.setData(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Import Receipt: #" + selected.getImportID());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbImport.getScene().getWindow());
            stage.showAndWait();
            
            loadImportData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}