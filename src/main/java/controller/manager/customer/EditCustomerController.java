package controller.manager.customer;

import dao.manager.customer.CustomerDAO;
import model.manager.customer.Customer;
import java.net.URL;
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
    @FXML private Label lblPoints;
    @FXML private Label lblRegisDate;
    @FXML private Label title;

    private CustomerDAO customerDAO = new CustomerDAO();
    private Customer currentCustomer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ràng buộc chỉ cho nhập số vào ô Phone
        txtPhone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtPhone.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    public void initData(Customer customer) {
        this.currentCustomer = customer;
        
        txtId.setText(String.valueOf(customer.getCustomerID()));
        txtName.setText(customer.getFullName());
        txtPhone.setText(customer.getPhoneNumber());
        txtEmail.setText(customer.getEmail());
        
        lblPoints.setText(customer.getPoints() + " pts");
        lblRegisDate.setText(customer.getRegistrationDate() != null ? 
                             customer.getRegistrationDate().toString() : "N/A");
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        
        // 2. Kiểm tra định dạng số điện thoại (10 số, bắt đầu bằng 0)
        if (!phone.matches("^0\\d{9}$")) {
            showAlert(Alert.AlertType.WARNING, "Format Error", "Phone Number must be 10 digits and start with 0.");
            return;
        }

        // 3. Kiểm tra Phone Unique (Loại trừ ID hiện tại)
        if (customerDAO.isPhoneExistsExcludeId(phone, currentCustomer.getCustomerID())) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error", "This phone number is already registered to another customer!");
            return;
        }

        // 4. Cập nhật dữ liệu
        currentCustomer.setFullName(name);
        currentCustomer.setPhoneNumber(phone);
        currentCustomer.setEmail(email);

        if (customerDAO.updateCustomer(currentCustomer)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");
            closeStage(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update customer information.");
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