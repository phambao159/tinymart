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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class for adding a new Size
 */
public class AddSizeController implements Initializable {

    @FXML
    private TextField txtType;

    private final SizeDAO sizeDAO = new SizeDAO();
    @FXML
    private Label title;

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

        // Reset style về mặc định trước khi kiểm tra
        txtType.setStyle("");

        // 1. Kiểm tra Not Null (Trống)
        if (type.isEmpty()) {
            txtType.setStyle("-fx-border-color: #e74c3c; -fx-focus-color: #e74c3c;");
            showAlert(Alert.AlertType.ERROR, "Input Error", "Size type cannot be empty!");
            txtType.requestFocus();
            return;
        }

        // 2. Kiểm tra Unique (Duy nhất)
        // Giả sử bạn thêm hàm isTypeExists(String type) vào SizeDAO
        if (sizeDAO.isTypeExists(type)) {
            txtType.setStyle("-fx-border-color: #e74c3c; -fx-focus-color: #e74c3c;");
            showAlert(Alert.AlertType.ERROR, "Duplicate Error", "This size type already exists!");
            txtType.requestFocus();
            return;
        }

        // 3. Thực hiện lưu nếu mọi thứ hợp lệ
        Size newSize = new Size(0, type, "Active");
        if (sizeDAO.insert(newSize)) {
            // Thông báo thành công (có thể dùng Toast hoặc Alert)
            closeWindow(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Failure", "Database error. Please try again!");
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
