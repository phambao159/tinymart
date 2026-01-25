package controller.manager.supplier;

import dao.manager.supplier.SupplierDAO;
import model.manager.supplier.Supplier;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditSupplierController implements Initializable {

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtContactPerson;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtAddress;

    private SupplierDAO supplierDAO;
    private Supplier currentSupplier;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        supplierDAO = new SupplierDAO();
        
        // Thêm Listener để xóa viền đỏ ngay khi người dùng bắt đầu nhập lại
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

    public void initData(Supplier supplier) {
        this.currentSupplier = supplier;
        txtId.setText(String.valueOf(supplier.getSupplierID()));
        txtName.setText(supplier.getName());
        txtContactPerson.setText(supplier.getContactPerson());
        txtPhone.setText(supplier.getPhoneNumber());
        txtAddress.setText(supplier.getAddress());
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        resetStyles();

        String name = txtName.getText().trim();
        String contact = txtContactPerson.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        int currentId = Integer.parseInt(txtId.getText());

        // 1. Kiểm tra trống
        if (name.isEmpty()) { showError(txtName, "Supplier Name cannot be empty!"); return; }
        if (contact.isEmpty()) { showError(txtContactPerson, "Contact Person cannot be empty!"); return; }
        if (phone.isEmpty()) { showError(txtPhone, "Phone Number cannot be empty!"); return; }
        if (address.isEmpty()) { showError(txtAddress, "Address cannot be empty!"); return; }

        // 2. Kiểm tra định dạng số điện thoại
        if (!phone.matches("\\d{9,11}")) {
            showError(txtPhone, "Phone number must be numeric and between 9-11 digits!");
            return;
        }

        // 3. Kiểm tra trùng số điện thoại (loại trừ ID hiện tại)
        if (supplierDAO.isPhoneExists(phone, currentId)) {
            showError(txtPhone, "This phone number is already assigned to another supplier!");
            return;
        }

        // 4. Tiến hành cập nhật
        Supplier supplier = new Supplier(currentId, name, contact, phone, address);
        boolean success = supplierDAO.updateSupplier(supplier);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier updated successfully!");
            closeStage(event); // Đóng form sau khi lưu thành công
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Update failed. Please try again.");
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Are you sure you want to delete this supplier?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (supplierDAO.deleteSupplier(currentSupplier.getSupplierID())) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Supplier removed successfully.");
                closeStage(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete this supplier (It might be linked to import receipts).");
            }
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        closeStage(event);
    }

    private void showError(Control field, String message) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
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
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}