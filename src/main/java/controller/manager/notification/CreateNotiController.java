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

    public void setExternalData(String title, String content, String targetRole) {
        txtTitle.setText(title);
        txtContent.setText(content);

        // Tự động tìm và chọn nhân viên đầu tiên có role là targetRole trong ComboBox
        if (cbReceiver.getItems() != null) {
            for (Employee emp : cbReceiver.getItems()) {
                if (targetRole.equalsIgnoreCase(emp.getRole())) {
                    cbReceiver.getSelectionModel().select(emp);
                    break; // Dừng lại khi tìm thấy người đầu tiên
                }
            }
        }
    }

    @FXML
    private void onSend(ActionEvent event) {
        // 1. Reset style trước khi check (xóa màu đỏ cũ nếu có)
        resetValidationStyles();

        // 2. Lấy dữ liệu
        Employee selectedEmp = cbReceiver.getSelectionModel().getSelectedItem();
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();

        // 3. Kiểm tra từng field (Validation)
        StringBuilder errorMsg = new StringBuilder();

        if (selectedEmp == null) {
            errorMsg.append("- Please select a receiver.\n");
            cbReceiver.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }

        if (title.isEmpty()) {
            errorMsg.append("- Subject (Title) cannot be empty.\n");
            txtTitle.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }

        if (content.isEmpty()) {
            errorMsg.append("- Content cannot be empty.\n");
            txtContent.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }

        // Nếu có lỗi thì hiện Alert và dừng lại
        if (errorMsg.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "The following fields are required:\n" + errorMsg.toString());
            return;
        }

        // 4. Logic lưu vào Database (Giữ nguyên phần xử lý của bạn)
        try {
            Notification n = new Notification();
            n.setEmployeeID(1); // Manager
            n.setReceiverID(selectedEmp.getEmployeeID());
            n.setTitle(title);
            n.setContent(content);
            n.setSentDate(new Date());
            n.setIsRead(false);

            dao.insert(n);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Notification sent successfully!");
            onCancel(event);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not send notification.");
        }
    }

    /**
     * Xóa các cảnh báo màu đỏ trên giao diện
     */
    private void resetValidationStyles() {
        cbReceiver.setStyle(null);
        txtTitle.setStyle(null);
        txtContent.setStyle(null);
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
