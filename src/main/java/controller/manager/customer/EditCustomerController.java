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
    @FXML private TextField txtPoints;
    @FXML private DatePicker dpRegistration;

    private CustomerDAO customerDAO = new CustomerDAO();
    private Customer currentCustomer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    // Nhận dữ liệu từ màn hình quản lý chính
    public void initData(Customer customer) {
        this.currentCustomer = customer;
        txtId.setText(String.valueOf(customer.getCustomerID()));
        txtName.setText(customer.getFullName());
        txtPhone.setText(customer.getPhoneNumber());
        txtEmail.setText(customer.getEmail());
        txtPoints.setText(String.valueOf(customer.getPoints()));
        dpRegistration.setValue(customer.getRegistrationDate());
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        try {
            currentCustomer.setFullName(txtName.getText().trim());
            currentCustomer.setPhoneNumber(txtPhone.getText().trim());
            currentCustomer.setEmail(txtEmail.getText().trim());
            currentCustomer.setPoints(Integer.parseInt(txtPoints.getText().trim()));
            currentCustomer.setRegistrationDate(dpRegistration.getValue());

            if (currentCustomer.getFullName().isEmpty() || currentCustomer.getPhoneNumber().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Name and Phone cannot be empty!");
                return;
            }

            if (customerDAO.updateCustomer(currentCustomer)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");
                closeStage(event);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Points must be a valid number!");
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete customer: " + currentCustomer.getFullName() + "?");
        confirm.setContentText("This action will remove the customer permanently.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (customerDAO.deleteCustomer(currentCustomer.getCustomerID())) {
                closeStage(event);
            }
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