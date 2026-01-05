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
    private TextField txtType; // Chuyển thành TextField
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Khởi tạo các lựa chọn cho trạng thái
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive", "Expired"));
    }

    /**
     * Nhận dữ liệu từ bảng ở PromotionController
     */
    public void initData(Promotion promotion) {
        this.selectedPromotion = promotion;

        txtID.setText(String.valueOf(promotion.getPromotionID()));
        txtName.setText(promotion.getName());
        txtDescription.setText(promotion.getDescription());
        txtType.setText(promotion.getType()); // Gán giá trị vào TextField
        txtValue.setText(String.valueOf(promotion.getValue()));
        dpStart.setValue(promotion.getStartDate());
        dpEnd.setValue(promotion.getEndDate());
        cbStatus.setValue(promotion.getStatus());
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            // 1. Kiểm tra dữ liệu
            String name = txtName.getText().trim();
            String type = txtType.getText().trim();
            String valueStr = txtValue.getText().trim();
            LocalDate start = dpStart.getValue();
            LocalDate end = dpEnd.getValue();

            if (name.isEmpty() || type.isEmpty() || valueStr.isEmpty() || start == null || end == null) {
                showAlert(Alert.AlertType.ERROR, "Form Error", "Please fill in all required fields!");
                return;
            }

            if (end.isBefore(start)) {
                showAlert(Alert.AlertType.ERROR, "Date Error", "End date must be after start date!");
                return;
            }

            // 2. Cập nhật đối tượng
            selectedPromotion.setName(name);
            selectedPromotion.setDescription(txtDescription.getText().trim());
            selectedPromotion.setType(type);
            selectedPromotion.setValue(Double.parseDouble(valueStr));
            selectedPromotion.setStartDate(start);
            selectedPromotion.setEndDate(end);
            selectedPromotion.setStatus(cbStatus.getValue());

            // 3. Gọi DAO cập nhật database
            if (promotionDAO.update(selectedPromotion)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Promotion updated successfully!");
                closeWindow(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update promotion.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Value must be a valid number!");
        }
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
