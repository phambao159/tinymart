package controller.Warehouse;

import dao.Warehouse.ImportDetailDAO;
import dao.Warehouse.ImportDAO;
import model.Warehouse.Imports;
import model.Warehouse.ImportDetail;
import util.DBConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ImportDetailController {

    @FXML
    private TextField txtNguoiXuat;
    @FXML
    private TextField txtMaNguon;
    @FXML
    private TextField txtKhoXuat;
    @FXML
    private TextField txtLyDo;
    @FXML
    private TextField txtTongTien;
    @FXML
    private Button btnConfirm;
    @FXML
    private TableView<ImportDetail> tbImportDetail;
    @FXML
    private TableColumn<ImportDetail, Integer> colNo;
    @FXML
    private TableColumn<ImportDetail, Integer> colImportDetailID;
    @FXML
    private TableColumn<ImportDetail, String> colProductID;
    @FXML
    private TableColumn<ImportDetail, String> colSizeID;
    @FXML
    private TableColumn<ImportDetail, Long> colQuantity;
    @FXML
    private TableColumn<ImportDetail, Double> colImportPrice;
    @FXML
    private TableColumn<ImportDetail, String> colExpiryDate; // nhập chuỗi dd-MM-yyyy

    private Imports importData;
    private ImportDetailDAO importDetailDAO;
    private ImportDAO importDAO;

    public void setImportData(Imports imp) {
        this.importData = imp;

        // Kết nối DB
        DBConnection db = new DBConnection();
        Connection conn = db.getConnect();
        importDetailDAO = new ImportDetailDAO(conn);
        importDAO = new ImportDAO(conn);

        // Lấy tên Employee và Supplier
        String employeeName = importDAO.getEmployeeNameById(imp.getEmployeeID());
        String supplierName = importDAO.getSupplierNameById(imp.getSupplierID());

        // Hiển thị thông tin chung
        txtNguoiXuat.setText(employeeName);
        txtMaNguon.setText(supplierName);
        txtKhoXuat.setText("Main Warehouse");
        txtLyDo.setText(imp.getStatus());
        txtTongTien.setText(String.valueOf(imp.getTotalCost()));

        boolean isCompleted = "Completed".equalsIgnoreCase(imp.getStatus());
        btnConfirm.setDisable(isCompleted);
        tbImportDetail.setEditable(!isCompleted);

        // Setup columns
        colImportDetailID.setCellValueFactory(new PropertyValueFactory<>("importDetailID"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colImportPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice"));
        colProductID.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSizeID.setCellValueFactory(new PropertyValueFactory<>("sizeType"));

        // ExpiryDate nhập chuỗi dd-MM-yyyy
        colExpiryDate.setCellValueFactory(new PropertyValueFactory<>("expiryDateString"));
        colExpiryDate.setCellFactory(TextFieldTableCell.forTableColumn());
        colExpiryDate.setOnEditCommit(event -> {
            ImportDetail detail = event.getRowValue();
            String newValue = event.getNewValue();
            try {
                LocalDate parsedDate = LocalDate.parse(newValue, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                detail.setExpiryDate(parsedDate);
                detail.setExpiryDateString(newValue);
            } catch (DateTimeParseException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ngày không hợp lệ! Vui lòng nhập theo định dạng dd-MM-yyyy.");
                alert.showAndWait();
            }
            tbImportDetail.refresh();
        });

        // Cột No. hiển thị số thứ tự
        colNo.setCellFactory(col -> new TableCell<ImportDetail, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        // Load dữ liệu chi tiết
        ObservableList<ImportDetail> details = FXCollections.observableArrayList(
                importDetailDAO.getDetailsByImportId(imp.getImportID())
        );
        tbImportDetail.setItems(details);
    }

    @FXML
    private void onConfirm() {
        if (importData != null) {
            // Kiểm tra tất cả ExpiryDate đã nhập
            boolean allDatesFilled = tbImportDetail.getItems().stream()
                    .allMatch(detail -> detail.getExpiryDate() != null);

            if (!allDatesFilled) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "Vui lòng nhập ExpiryDate cho tất cả sản phẩm trước khi xác nhận!");
                alert.showAndWait();
                return;
            }

            // Cập nhật ExpiryDate vào DB
            boolean expiryUpdated = true;
            for (ImportDetail detail : tbImportDetail.getItems()) {
                boolean updated = importDetailDAO.updateExpiryDate(detail.getImportDetailID(), detail.getExpiryDate());
                if (!updated) {
                    expiryUpdated = false;
                    break;
                }
            }

            if (!expiryUpdated) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Cập nhật ExpiryDate thất bại!");
                alert.showAndWait();
                return;
            }

            // Sau khi cập nhật ExpiryDate thành công -> cập nhật Status
            boolean success = importDAO.updateStatus(importData.getImportID(), "Completed");
            if (success) {
                txtLyDo.setText("Completed");
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Xác nhận thành công!");
                alert.showAndWait();
                btnConfirm.setDisable(true);
                tbImportDetail.setEditable(false);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update status!");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) txtNguoiXuat.getScene().getWindow();
        stage.close();
    }
}