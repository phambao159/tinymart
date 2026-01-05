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

    private CategoryDAO categoryDAO = new CategoryDAO();
    private Category selectedCategory;

    // Hàm này dùng để truyền dữ liệu từ màn hình chính sang form edit
    public void initData(Category category) {
        this.selectedCategory = category;
        txtID.setText(String.valueOf(category.getCategoryID()));
        txtName.setText(category.getName());
        txtDescription.setText(category.getDescription());
    }

    @FXML
    private void onSave(ActionEvent event) {
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name cannot be empty!");
            return;
        }

        selectedCategory.setName(name);
        selectedCategory.setDescription(description);

        if (categoryDAO.update(selectedCategory)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Update successful!");
            closeWindow(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Update failed!");
        }
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
                closeWindow(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete! This category might be in use.");
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
        alert.setContentText(content);
        alert.showAndWait();
    }
}   