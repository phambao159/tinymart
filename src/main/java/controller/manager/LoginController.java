package controller.manager;

import dao.manager.EmployeeDAO;
import main.App;
import model.manager.Employee;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import controller.cashier.CashierController;

public class LoginController implements Initializable {

    @FXML
    private HBox mainLogin;
    @FXML
    private HBox hBoxImage;
    @FXML
    private Label lbLogin;
    @FXML
    private TextField user;
    @FXML
    private PasswordField password;

    private EmployeeDAO employeeDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        employeeDAO = new EmployeeDAO();
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = user.getText().trim();
        String pwd = password.getText().trim();

        if (username.isEmpty() || pwd.isEmpty()) {
            showAlert("Warning", "User - Password can't be emp", AlertType.WARNING);
            return;
        }

        try {
            String empID = employeeDAO.authenticate(username, pwd);

            if (empID != null) {
                List<Employee> listEmp = employeeDAO.getData();

                if (listEmp == null) {
                    throw new Exception("Error");
                }
                String role = "";
                Employee foundEmp = null;
                for (Employee emp : listEmp) {
                    if (emp.getUser() != null && emp.getUser().equalsIgnoreCase(username)) {
                        foundEmp = emp;
                        role = emp.getRole();
                        break;
                    }
                }

                if (foundEmp == null) {
                    App.setRoot("manager", "layout");
                    return;
                }

                // 3. Điều hướng
                if ("Employee".equalsIgnoreCase(role)) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashier/cashier.fxml"));
                        Parent root = loader.load();

                        CashierController cashierCtrl = loader.getController();
                        if (empID != null) {
                            cashierCtrl.setEmployeeID(Integer.parseInt(empID));
                        }
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(scene);
                        stage.setTitle("TinyMart - Cashier System");
                        stage.centerOnScreen();
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new Exception("Lỗi khi load file FXML: " + e.getMessage());
                    }
                } else {
                    App.setRoot("manager", "layout");
                }

            } else {
                showAlert("Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu.", AlertType.ERROR);
                password.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("TinyMart - " + title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
