package controller.manager.product;

import dao.manager.product.SizeDAO;
import model.manager.product.Size;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class for adding a new Size
 */
public class AddSizeController implements Initializable {

    @FXML
    private TextField txtType;

    private final SizeDAO sizeDAO = new SizeDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization logic if needed
    }    

    /**
     * Handles Save button action
     */
    @FXML
    private void onSave(ActionEvent event) {
        String type = txtType.getText().trim();

        // 1. Input Validation
        if (type.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter the size type!");
            return;
        }

        // 2. Create Model object (SizeID is 0 as it's auto-incremented in DB)
        Size newSize = new Size(0, type);

        // 3. Call DAO to perform database insertion
        if (sizeDAO.insert(newSize)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "New size added successfully!");
            closeWindow(event); // Close form after successful save
        } else {
            showAlert(Alert.AlertType.ERROR, "Failure", "Could not add size. Please try again!");
        }
    }

    /**
     * Handles Cancel button action
     */
    @FXML
    private void onCancel(ActionEvent event) {
        closeWindow(event);
    }

    /**
     * Helper to close the current window
     */
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Helper to display alerts
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}