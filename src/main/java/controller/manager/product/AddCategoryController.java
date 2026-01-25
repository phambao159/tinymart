package controller.manager.product;

import dao.manager.product.CategoryDAO;
import model.manager.product.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddCategoryController {

    @FXML
    private TextField txtName;
    @FXML
    private TextArea txtDescription;

    private CategoryDAO categoryDAO = new CategoryDAO();
    private Runnable refreshCallback;

    // Callback để load lại bảng ở màn hình danh sách sau khi thêm thành công
    public void setOnSave(Runnable callback) {
        this.refreshCallback = callback;
    }

    @FXML
    private void onSave(ActionEvent event) {
        resetStyles(); // Xóa viền đỏ cũ

        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();

        // 1. Kiểm tra Not Null (Chỉ yêu cầu Name)
        if (name.isEmpty()) {
            showError(txtName, "Category name cannot be empty!");
            return;
        }

        // 2. Kiểm tra trùng tên trong Database
        // Sử dụng hàm isNameExists (hàm cũ của bạn) vì đây là thêm mới hoàn toàn
        if (categoryDAO.isNameExists(name)) {
            showError(txtName, "This category name already exists!");
            return;
        }

        // 3. Tạo đối tượng mới (Description có thể null hoặc trống)
        // Mặc định trạng thái là "Active"
        Category newCat = new Category(0, name, description.isEmpty() ? null : description, "Active");
        
        if (categoryDAO.insert(newCat)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category added successfully!");
            if (refreshCallback != null) refreshCallback.run();
            closeWindow(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add category.");
        }
    }

    // --- Helpers để bôi đỏ và hiển thị lỗi ---

    private void showError(Control control, String message) {
        control.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5px;");
        control.requestFocus();
        showAlert(Alert.AlertType.ERROR, "Validation Error", message);
    }

    private void resetStyles() {
        txtName.setStyle("");
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