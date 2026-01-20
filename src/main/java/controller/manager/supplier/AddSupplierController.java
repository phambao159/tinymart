package controller.manager.supplier;

import dao.manager.supplier.SupplierDAO;
import model.manager.supplier.Supplier;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddSupplierController implements Initializable {

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtContactPerson;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextArea txtAddress;

    private SupplierDAO supplierDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        supplierDAO = new SupplierDAO();
    }    

    @FXML
    private void onSave(ActionEvent event) {
        // 1. Lấy dữ liệu từ các trường nhập liệu
        String name = txtName.getText().trim();
        String contact = txtContactPerson.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        // 2. Kiểm tra dữ liệu (Validation)
        if (name.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Supplier Name and Phone Number are required!");
            return;
        }

        // 3. Tạo đối tượng Supplier mới (Dùng Constructor không có ID)
        Supplier newSupplier = new Supplier(name, contact, phone, address);

        // 4. Gọi DAO để lưu vào Database
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

    // Hàm hỗ trợ đóng cửa sổ hiện tại
    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    // Hàm hỗ trợ hiển thị thông báo
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}