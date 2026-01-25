package controller.manager.product;

import dao.manager.product.PromotionDAO;
import model.manager.product.Promotion;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditPromotionController implements Initializable {

    @FXML
    private TextField txtID;
    @FXML
    private TextField txtName;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtValue;
    @FXML
    private DatePicker dpStart;
    @FXML
    private DatePicker dpEnd;
    @FXML
    private ComboBox<String> cbStatus;

    private final PromotionDAO promotionDAO = new PromotionDAO();
    private Promotion selectedPromotion;

    @FXML
    private Label title;
    @FXML
    private ComboBox<String> cbType;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Khởi tạo các lựa chọn cho trạng thái
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive", "Expired"));
        cbType.setItems(FXCollections.observableArrayList("Fixed Discount", "BOGO"));
    }

    /**
     * Nhận dữ liệu từ bảng ở PromotionController
     */
    public void initData(Promotion promotion) {
        this.selectedPromotion = promotion;

        txtID.setText(String.valueOf(promotion.getPromotionID()));
        txtName.setText(promotion.getName());
        txtDescription.setText(promotion.getDescription());
        cbType.setValue(promotion.getType());
        txtValue.setText(String.valueOf(promotion.getValue()));
        dpStart.setValue(promotion.getStartDate());
        dpEnd.setValue(promotion.getEndDate());
        cbStatus.setValue(promotion.getStatus());
    }

    @FXML
    private void onSave(ActionEvent event) {
        // 1. Reset styles về mặc định
        resetStyles();

        try {
            // Lấy dữ liệu từ giao diện
            String name = txtName.getText().trim();
            String description = txtDescription.getText().trim();
            String type = cbType.getValue();
            String valueStr = txtValue.getText().trim();
            LocalDate start = dpStart.getValue();
            LocalDate end = dpEnd.getValue();
            String status = cbStatus.getValue();

            // 2. Kiểm tra Not Null và Validation cho từng trường
            if (name.isEmpty()) {
                showError(txtName, "Promotion name cannot be empty!");
                return;
            }


            if (type == null) {
                showError(cbType, "Please select a promotion type!");
                return;
            }

            // Kiểm tra Value
            double value;
            try {
                if (valueStr.isEmpty()) {
                    throw new Exception("Value cannot be empty!");
                }
                value = Double.parseDouble(valueStr);
                if (value <= 0) {
                    throw new Exception("Value must be greater than 0!");
                }
            } catch (Exception e) {
                showError(txtValue, e instanceof NumberFormatException ? "Value must be a valid number!" : e.getMessage());
                return;
            }

            if (start == null) {
                showError(dpStart, "Please select a start date!");
                return;
            }

            if (end == null) {
                showError(dpEnd, "Please select an end date!");
                return;
            }

            // Kiểm tra logic ngày tháng
            if (end.isBefore(start)) {
                showError(dpEnd, "End date cannot be earlier than start date!");
                return;
            }

            if (status == null) {
                showError(cbStatus, "Please select a status!");
                return;
            }

            // 3. Cập nhật đối tượng và gọi DAO
            selectedPromotion.setName(name);
            selectedPromotion.setDescription(description);
            selectedPromotion.setType(type);
            selectedPromotion.setValue(value);
            selectedPromotion.setStartDate(start);
            selectedPromotion.setEndDate(end);
            selectedPromotion.setStatus(status);

            if (promotionDAO.update(selectedPromotion)) {
                promotionDAO.forceUpdateExpired(); // Cập nhật trạng thái hết hạn nếu cần
                closeWindow(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update promotion.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Hiển thị lỗi: Đổi màu viền và thông báo
     */
    private void showError(Control control, String message) {
        control.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5px;");
        control.requestFocus();
        showAlert(Alert.AlertType.ERROR, "Validation Error", message);
    }

    /**
     * Xóa bỏ các đánh dấu lỗi trước đó
     */
    private void resetStyles() {
        txtName.setStyle("");
        txtDescription.setStyle("");
        cbType.setStyle("");
        txtValue.setStyle("");
        dpStart.setStyle("");
        dpEnd.setStyle("");
        cbStatus.setStyle("");
    }

    @FXML
    private void onDelete(ActionEvent event) {
        // Xác nhận trước khi xóa
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Promotion: " + selectedPromotion.getName() + "?");
        alert.setContentText("This action cannot be undone. Are you sure?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (promotionDAO.delete(selectedPromotion.getPromotionID())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Promotion deleted successfully!");
                closeWindow(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not delete. This promotion may be linked to other records.");
            }
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
