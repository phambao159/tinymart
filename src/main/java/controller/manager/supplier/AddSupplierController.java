package controller.manager.supplier;

import dao.manager.supplier.SupplierDAO;
import model.manager.supplier.Supplier;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddSupplierController implements Initializable {

    @FXML private TextField txtName;
    @FXML private TextField txtContactPerson;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtAddress;

    private SupplierDAO supplierDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        supplierDAO = new SupplierDAO();
        
        // Thêm Listeners để tự động xóa viền đỏ khi người dùng gõ lại
        addTextListener(txtName);
        addTextListener(txtContactPerson);
        addTextListener(txtPhone);
        addTextListener(txtAddress);
    }

    private void addTextListener(Control field) {
        if (field instanceof TextField) {
            ((TextField) field).textProperty().addListener((obs, oldV, newV) -> field.setStyle(""));
        } else if (field instanceof TextArea) {
            ((TextArea) field).textProperty().addListener((obs, oldV, newV) -> field.setStyle(""));
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        // Xóa style lỗi cũ
        resetStyles();

        String name = txtName.getText().trim();
        String contact = txtContactPerson.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        // 1. Kiểm tra Not Null / Empty cho từng trường
        if (name.isEmpty()) {
            showError(txtName, "Supplier Name cannot be empty!");
            return;
        }
        if (contact.isEmpty()) {
            showError(txtContactPerson, "Contact Person cannot be empty!");
            return;
        }
        if (phone.isEmpty()) {
            showError(txtPhone, "Phone Number cannot be empty!");
            return;
        }
        if (address.isEmpty()) {
            showError(txtAddress, "Address cannot be empty!");
            return;
        }

        // 2. Kiểm tra định dạng số điện thoại (9-11 chữ số)
        if (!phone.matches("\\d{9,11}")) {
            showError(txtPhone, "Phone number must be numeric and between 9-11 digits!");
            return;
        }

        // 3. Kiểm tra trùng số điện thoại trong Database
        // Vì là Add New nên ID truyền vào là -1 (hoặc 0) để hàm isPhoneExists chỉ check phone
        if (supplierDAO.isPhoneExists(phone, -1)) {
            showError(txtPhone, "This phone number is already registered to another supplier!");
            return;
        }

        // 4. Tiến hành lưu
        Supplier newSupplier = new Supplier(name, contact, phone, address);
        try {
            boolean success = supplierDAO.addSupplier(newSupplier);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier added successfully!");
                closeStage(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add supplier.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        closeStage(event);
    }

    // Hàm hiển thị lỗi chuyên nghiệp
    private void showError(Control field, String message) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 3px;");
        showAlert(Alert.AlertType.WARNING, "Validation Error", message);
        field.requestFocus();
    }

    private void resetStyles() {
        txtName.setStyle("");
        txtContactPerson.setStyle("");
        txtPhone.setStyle("");
        txtAddress.setStyle("");
    }

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
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