package controller.manager.product;

import dao.manager.product.CategoryDAO;
import model.manager.product.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Optional;

public class EditCategoryController {

    @FXML
    private TextField txtID;
    @FXML
    private TextField txtName;
    @FXML
    private TextArea txtDescription;
    @FXML
    private Label title;

    private CategoryDAO categoryDAO = new CategoryDAO();
    private Category selectedCategory;
    private Runnable refreshCallback;

    public void setOnSave(Runnable callback) {
        this.refreshCallback = callback;
    }

    public void initData(Category category) {
        this.selectedCategory = category;
        txtID.setText(String.valueOf(category.getCategoryID()));
        txtName.setText(category.getName());
        txtDescription.setText(category.getDescription());
    }

    @FXML
    private void onSave(ActionEvent event) {
        resetStyles(); // Xóa viền đỏ cũ

        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        int currentID = selectedCategory.getCategoryID();

        // 1. Kiểm tra Not Null cho Name
        if (name.isEmpty()) {
            showError(txtName, "Category name cannot be empty!");
            return;
        }

        // 2. Kiểm tra trùng tên (loại trừ chính nó)
        // Lưu ý: Bạn cần thêm hàm isNameExistsForEdit vào CategoryDAO tương tự như ProductDAO
        if (categoryDAO.isNameExistsForEdit(name, currentID)) {
            showError(txtName, "This category name already exists!");
            return;
        }

        // 3. Cập nhật dữ liệu (Description có thể để trống)
        selectedCategory.setName(name);
        selectedCategory.setDescription(description.isEmpty() ? null : description);

        if (categoryDAO.update(selectedCategory)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Update successful!");
            if (refreshCallback != null) refreshCallback.run();
            closeWindow(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Update failed!");
        }
    }

    // --- Helpers để bôi đỏ và reset style ---
    
    private void showError(Control control, String message) {
        control.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5px;");
        control.requestFocus();
        showAlert(Alert.AlertType.ERROR, "Validation Error", message);
    }

    private void resetStyles() {
        txtName.setStyle("");
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this category?");
        alert.setContentText("Name: " + selectedCategory.getName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (categoryDAO.delete(selectedCategory.getCategoryID())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted!");
                if (refreshCallback != null) refreshCallback.run();
                closeWindow(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete! This category might be in use by products.");
            }
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
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