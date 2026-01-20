package controller.manager.notification;

import dao.manager.employee.EmployeeDAO;
import dao.manager.notification.NotificationDAO;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.manager.employee.Employee;
import model.manager.notification.Notification;

public class CreateNotiController implements Initializable {

    @FXML
    private TextField txtTitle;
    @FXML
    private TextArea txtContent;
    @FXML
    private ComboBox<Employee> cbReceiver;

    private final NotificationDAO dao = new NotificationDAO();
    private final EmployeeDAO eDAO = new EmployeeDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadReceiver();
    }

    @FXML
    private void onSend(ActionEvent event) {
        // 1. Lấy đối tượng Employee được chọn từ ComboBox
        Employee selectedEmp = cbReceiver.getSelectionModel().getSelectedItem();
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();

        // 2. Kiểm tra dữ liệu trống
        if (selectedEmp == null || title.isEmpty() || content.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        try {
            // 3. Tạo đối tượng Notification trực tiếp từ dữ liệu đã chọn
            Notification n = new Notification();
            n.setEmployeeID(1); // ID người gửi (Manager)
            n.setReceiverID(selectedEmp.getEmployeeID()); // Lấy ID từ đối tượng Employee
            n.setTitle(title);
            n.setContent(content);
            n.setSentDate(new Date());
            n.setIsRead(false);

            // 4. Lưu vào Database
            dao.insert(n);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Notification sent successfully!");
            onCancel(event);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not send notification: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }

    private void loadReceiver() {
        try {
            // 1. Lấy dữ liệu từ database
            List<Employee> empList = eDAO.getData();

            // 2. Tạo danh sách mới để lọc (Bỏ Manager có ID = 1)
            ObservableList<Employee> filteredList = FXCollections.observableArrayList();
            for (Employee emp : empList) {
                // Loại bỏ ID = 1 (Người gửi) khỏi danh sách chọn
                if (emp.getEmployeeID() != 1) {
                    filteredList.add(emp);
                }
            }

            // 3. Đưa danh sách đã lọc vào ComboBox
            cbReceiver.setItems(filteredList);

            // 4. Thiết lập cách hiển thị (StringConverter chỉ làm nhiệm vụ hiển thị)
            cbReceiver.setConverter(new StringConverter<Employee>() {
                @Override
                public String toString(Employee emp) {
                    if (emp == null) {
                        return "";
                    }
                    // Trả về định dạng: Tên (Chức vụ)
                    return emp.getFullName() + " (" + emp.getRole() + ")";
                }

                @Override
                public Employee fromString(String string) {
                    return null;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load employee list.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
