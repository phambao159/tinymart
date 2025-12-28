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

public class LoginController implements Initializable {

    @FXML private HBox mainLogin;
    @FXML private HBox hBoxImage;
    @FXML private Label lbLogin;
    @FXML private TextField user; 
    @FXML private PasswordField password; 

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
            showAlert("Cảnh báo", "Vui lòng nhập user và password!", AlertType.WARNING);
            return;
        }

        try {
            System.out.println("--- BẮT ĐẦU LOGIN ---");
            System.out.println("User nhập vào: " + username);

            // 1. Kiểm tra đăng nhập
            String empID = employeeDAO.authenticate(username, pwd);

            if (empID != null) {
                System.out.println("=> Bước 1: Authenticate thành công. ID trả về: " + empID);
                
                // 2. Lấy danh sách nhân viên để tìm Role
                System.out.println("=> Bước 2: Đang gọi hàm getData()...");
                List<Employee> listEmp = employeeDAO.getData();
                
                if (listEmp == null) {
                    throw new Exception("Hàm getData() trả về null (Lỗi kết nối hoặc query sai).");
                }
                System.out.println("=> Bước 3: Lấy được danh sách " + listEmp.size() + " nhân viên.");

                String role = "";
                Employee foundEmp = null;

                // Lọc tìm user
                for (Employee emp : listEmp) {
                    // In ra để kiểm tra xem dữ liệu lấy lên có bị null không
                    // System.out.println("Check row: User=" + emp.getUser() + ", Role=" + emp.getRole());
                    
                    if (emp.getUser() != null && emp.getUser().equalsIgnoreCase(username)) {
                        foundEmp = emp;
                        role = emp.getRole();
                        System.out.println("=> Bước 4: Đã tìm thấy User trong list. Role là: " + role);
                        break;
                    }
                }

                if (foundEmp == null) {
                     // Trường hợp authenticate trả về ID nhưng getData lại không tìm thấy User tương ứng
                     // Có thể do authenticate check bảng khác, hoặc getData query thiếu
                     System.out.println("=> LỖI: Không tìm thấy object Employee trong list tương ứng với user này.");
                     // Vẫn cho vào mặc định Manager nếu không tìm thấy, hoặc báo lỗi tùy bạn
                     App.setRoot("manager", "layout");
                     return;
                }

                // 3. Điều hướng
                if ("Employee".equalsIgnoreCase(role)) {
                    System.out.println("=> Bước 5: Đang mở giao diện Cashier...");
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/cashier/cashier.fxml"));
                        Parent root = loader.load();
                        
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(scene);
                        stage.setTitle("TinyMart - Cashier System");
                        stage.centerOnScreen();
                        stage.show();
                        System.out.println("=> Bước 6: Mở Cashier thành công!");
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new Exception("Lỗi khi load file FXML: " + e.getMessage());
                    }
                } else {
                    System.out.println("=> Bước 5: User là Manager, mở giao diện quản lý cũ.");
                    App.setRoot("manager", "layout"); 
                }
                        
            } else {
                showAlert("Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu.", AlertType.ERROR);
                password.clear();
            }
            
        } catch (Exception e) {
            // --- ĐÂY LÀ PHẦN QUAN TRỌNG ĐỂ BẮT LỖI ---
            e.printStackTrace(); // In lỗi đầy đủ ra cửa sổ Output bên dưới
            showAlert("LỖI CHI TIẾT", "Hệ thống gặp lỗi: " + e.toString(), AlertType.ERROR);
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