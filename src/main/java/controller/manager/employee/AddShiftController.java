package controller.manager.employee;

import dao.manager.employee.ShiftDAO;
import model.manager.employee.Shift;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddShiftController implements Initializable {

    @FXML
    private TextField txtShiftName;
    @FXML
    private TextField txtStartTime; // User enters HH:mm
    @FXML
    private TextField txtEndTime;   // User enters HH:mm

    private ShiftDAO shiftDAO = new ShiftDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // You could set default values here if desired
    }

    @FXML
    private void onSave(ActionEvent event) {
        // Reset lại style (xóa viền đỏ cũ nếu có)
        resetStyles();

        String name = txtShiftName.getText().trim();
        String startStr = txtStartTime.getText().trim();
        String endStr = txtEndTime.getText().trim();

        // 1. Kiểm tra Not Null & Validation từng trường
        StringBuilder errorMsg = new StringBuilder();

        if (name.isEmpty()) {
            errorMsg.append("- Shift Name is required.\n");
            txtShiftName.setStyle("-fx-border-color: red;");
        }
        if (startStr.isEmpty()) {
            errorMsg.append("- Start Time is required.\n");
            txtStartTime.setStyle("-fx-border-color: red;");
        }
        if (endStr.isEmpty()) {
            errorMsg.append("- End Time is required.\n");
            txtEndTime.setStyle("-fx-border-color: red;");
        }

        if (errorMsg.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fix the following errors:\n" + errorMsg.toString());
            return;
        }

        try {
            // 2. Kiểm tra Unique Name (Trùng tên ca làm)
            // Giả sử ShiftDAO có hàm check tên, nếu không bạn cần viết thêm hoặc lấy list về so sánh
            if (shiftDAO.isShiftNameExists(name)) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Entry", "The shift name '" + name + "' already exists. Please use a different name.");
                txtShiftName.setStyle("-fx-border-color: red;");
                return;
            }

            // 3. Parse và kiểm tra logic thời gian
            LocalTime startTime = LocalTime.parse(startStr);
            LocalTime endTime = LocalTime.parse(endStr);

            if (!startTime.isBefore(endTime)) {
                showAlert(Alert.AlertType.WARNING, "Invalid Schedule", "Start time must be earlier than end time.");
                return;
            }

            // 4. Lưu dữ liệu
            Shift newShift = new Shift(startTime, endTime, name);

            if (shiftDAO.addShift(newShift)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "New shift created successfully!");
                onClose(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save the shift.");
            }

        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Format", "Please use HH:mm format (e.g., 08:30).");
        }
    }

    private void resetStyles() {
        txtShiftName.setStyle(null);
        txtStartTime.setStyle(null);
        txtEndTime.setStyle(null);
    }

    @FXML
    private void onClose(ActionEvent event) {
        ((Stage) txtShiftName.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
