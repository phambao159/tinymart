package controller.manager.customer;

import dao.manager.customer.CustomerDAO;
import model.manager.customer.Customer;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditCustomerController implements Initializable {

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    
    // Đã cập nhật thành Label theo FXML mới
    @FXML private Label lblPoints;
    @FXML private Label lblRegisDate;

    private CustomerDAO customerDAO = new CustomerDAO();
    private Customer currentCustomer;
    @FXML
    private Label title;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Có thể thêm filter chỉ cho nhập số ở txtPhone tại đây nếu cần
    }

    /**
     * Nhận dữ liệu từ màn hình quản lý chính và đổ vào các Control
     */
    public void initData(Customer customer) {
        this.currentCustomer = customer;
        
        // Đổ dữ liệu vào các ô nhập liệu
        txtId.setText(String.valueOf(customer.getCustomerID()));
        txtName.setText(customer.getFullName());
        txtPhone.setText(customer.getPhoneNumber());
        txtEmail.setText(customer.getEmail());
        
        // Đổ dữ liệu vào các Label (Dữ liệu chỉ đọc)
        lblPoints.setText(customer.getPoints() + " pts");
        lblRegisDate.setText(customer.getRegistrationDate() != null ? 
                             customer.getRegistrationDate().toString() : "N/A");
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        // 1. Lấy dữ liệu từ giao diện
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        // 2. Kiểm tra tính hợp lệ (Validation)
        if (name.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name and Phone Number are required!");
            return;
        }
        
        // Kiểm tra định dạng số điện thoại cơ bản (ví dụ)
        if (!phone.matches("\\d{10,11}")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid phone number format (10-11 digits).");
            return;
        }

        // 3. Cập nhật đối tượng hiện tại
        currentCustomer.setFullName(name);
        currentCustomer.setPhoneNumber(phone);
        currentCustomer.setEmail(email);
        // Lưu ý: Points và RegistrationDate không đổi vì không cho sửa trên giao diện

        // 4. Lưu xuống Database
        if (customerDAO.updateCustomer(currentCustomer)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");
            closeStage(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update customer information.");
        }
    }


    @FXML
    private void onCancel(ActionEvent event) {
        closeStage(event);
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