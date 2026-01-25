package controller.manager.product;

import dao.manager.product.SizeDAO;
import model.manager.product.Size;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditSizeController implements Initializable {

    @FXML
    private TextField txtID;
    @FXML
    private TextField txtType;

    private final SizeDAO sizeDAO = new SizeDAO();
    private Size selectedSize;
    @FXML
    private Label title;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * Nhận dữ liệu từ SizeController chính
     */
    public void initData(Size size) {
        this.selectedSize = size;
        txtID.setText(String.valueOf(size.getSizeID()));
        txtType.setText(size.getType());
    }

    @FXML
    private void onSave(ActionEvent event) {
        String type = txtType.getText().trim();

        // Reset style
        txtType.setStyle("");

        // 1. Kiểm tra Not Null
        if (type.isEmpty()) {
            txtType.setStyle("-fx-border-color: #e74c3c;");
            showAlert(Alert.AlertType.ERROR, "Input Error", "Size type cannot be empty!");
            txtType.requestFocus();
            return;
        }

        // 2. Kiểm tra Unique (Tránh trùng với các Size khác)
        // Nếu tên mới khác tên cũ, mới cần check unique trong database
        if (!type.equalsIgnoreCase(selectedSize.getType())) {
            if (sizeDAO.isTypeExists(type)) {
                txtType.setStyle("-fx-border-color: #e74c3c;");
                showAlert(Alert.AlertType.ERROR, "Duplicate Error", "This size type already exists!");
                txtType.requestFocus();
                return;
            }
        }

        // 3. Thực hiện cập nhật
        selectedSize.setType(type);
        if (sizeDAO.update(selectedSize)) {
            // Có thể dùng thông báo nhẹ hoặc đóng cửa sổ luôn
            closeWindow(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update size. Please try again.");
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Size: " + selectedSize.getType() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (sizeDAO.delete(selectedSize.getSizeID())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Size deleted successfully!");
                closeWindow(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete! This size might be linked to products.");
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
