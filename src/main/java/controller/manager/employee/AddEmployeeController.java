package controller.manager.employee;

import dao.manager.employee.EmployeeDAO;
import model.manager.employee.Employee;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddEmployeeController implements Initializable {

    @FXML private TextField txtName, txtPhone, txtAddress, txtSalary, txtUser;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker dpDob, dpHireDate;
    @FXML private ComboBox<String> cbRole;

    private EmployeeDAO employeeDAO = new EmployeeDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cấu hình ComboBox Role
        cbRole.setItems(FXCollections.observableArrayList("Manager", "Staff", "Warehouse"));
        
        // Mặc định ngày thuê là ngày hôm nay
        dpHireDate.setValue(LocalDate.now());
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            // 1. Kiểm tra input không được để trống
            if (isInputInvalid()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please fill in all fields!");
                return;
            }

            // 2. Lấy dữ liệu từ Form
            String fullName = txtName.getText().trim();
            LocalDate dob = dpDob.getValue();
            String phone = txtPhone.getText().trim();
            String address = txtAddress.getText().trim();
            String role = cbRole.getValue();
            long salary = Long.parseLong(txtSalary.getText().trim());
            String user = txtUser.getText().trim();
            String password = txtPassword.getText();
            LocalDate hireDate = dpHireDate.getValue();

            // 3. Tạo đối tượng Employee bằng Constructor mới
            Employee emp = new Employee(
                fullName, 
                dob, 
                phone, 
                address, 
                role, 
                hireDate, 
                salary, 
                user, 
                password
            );

            // 4. Gọi DAO lưu vào DB
            if (employeeDAO.insert(emp)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee added successfully!");
                onClose(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add employee. Check Username availability.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Salary must be a valid number!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    private boolean isInputInvalid() {
        return txtName.getText().isEmpty() || 
               dpDob.getValue() == null || 
               txtPhone.getText().isEmpty() || 
               txtUser.getText().isEmpty() || 
               txtPassword.getText().isEmpty() || 
               cbRole.getValue() == null ||
               txtSalary.getText().isEmpty();
    }

    @FXML
    private void onClose(ActionEvent event) {
        Stage stage = (Stage) txtName.getScene().getWindow();
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