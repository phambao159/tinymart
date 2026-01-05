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

    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPoints;
    @FXML private DatePicker dpRegistration;

    private CustomerDAO customerDAO = new CustomerDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mặc định ngày đăng ký là ngày hiện tại
        dpRegistration.setValue(LocalDate.now());
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            // 1. Thu thập dữ liệu
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            int points = Integer.parseInt(txtPoints.getText().trim());
            LocalDate regDate = dpRegistration.getValue();

            // 2. Kiểm tra tính hợp lệ
            if (name.isEmpty() || phone.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Name and Phone are required!");
                return;
            }

            // 3. Tạo model và lưu
            Customer customer = new Customer(0, name, phone, points, email, regDate);
            if (customerDAO.addCustomer(customer)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer added successfully!");
                closeStage(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save customer.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Points must be a number!");
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