package controller.manager.customer;

import dao.manager.customer.CustomerDAO;
import model.manager.customer.Customer;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddCustomerController implements Initializable {

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;

    private CustomerDAO customerDAO = new CustomerDAO();
    @FXML
    private Label title;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    private void onSave(ActionEvent event) {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        // 2. Kiểm tra định dạng số (Regex)
        // ^0: Bắt đầu bằng số 0, \\d{9}: theo sau là đúng 9 chữ số (tổng 10 số)
        if (!phone.matches("^0\\d{9}$")) {
            showAlert(Alert.AlertType.ERROR, "Format Error", "Phone Number must be 10 digits and start with 0!");
            return;
        }

        // 3. Kiểm tra trùng lặp (Unique)
        if (customerDAO.isPhoneExists(phone)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error", "This Phone Number is already registered!");
            return;
        }

        // Thực hiện lưu nếu qua hết các bước kiểm tra
        try {
            int points = 0;
            LocalDate regDate = LocalDate.now();
            Customer customer = new Customer(0, name, phone, points, email, regDate);

            if (customerDAO.addCustomer(customer)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer added successfully!");
                closeStage(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save customer.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "System Error", "An unexpected error occurred.");
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
