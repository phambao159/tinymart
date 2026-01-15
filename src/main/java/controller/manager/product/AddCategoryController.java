package controller.manager.product;

import dao.manager.product.CategoryDAO;
import model.manager.product.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddCategoryController {

    @FXML
    private TextField txtName;
    @FXML
    private TextArea txtDescription;

    private CategoryDAO categoryDAO = new CategoryDAO();

    @FXML
    private void onSave(ActionEvent event) {
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Error", "Please fill in all fields!");
            return;
        }

        // CategoryID tự tăng trong SQL nên ta để tạm là 0
        Category newCat = new Category(0, name, description,"Active");
        
        if (categoryDAO.insert(newCat)) {
            showAlert("Success", "Category added successfully!");
            closeWindow(event);
        } else {
            showAlert("Error", "Failed to add category.");
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}