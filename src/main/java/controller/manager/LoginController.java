package controller.manager;

import dao.manager.EmployeeDAO;
import main.App; 
import model.manager.Employee; 

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert; 
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;


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
            showAlert("Error Login", "Please enter username and password fully!", AlertType.WARNING);
            return;
        }

        try {
           
            String employee = employeeDAO.authenticate(username, pwd);

            if (employee != null) {
                App.setRoot("manager","layout"); 
                        
            } else {
            
                showAlert("Error Login", "Username or Password is not correct.", AlertType.ERROR);
               
                password.clear();
            }
            
        } catch (IOException e) {
            
            showAlert("Error System", "Not found Manager Dashboard", AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
           
            showAlert("Error Database", "Cannot connect to Database!", AlertType.ERROR);
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