package controller.manager.employee;

import dao.manager.employee.ShiftDAO;
import model.manager.employee.Shift;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditShiftController implements Initializable {

    @FXML private TextField txtShiftName;
    @FXML private TextField txtStartTime;
    @FXML private TextField txtEndTime;

    private ShiftDAO shiftDAO = new ShiftDAO();
    private Shift selectedShift;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void initData(Shift shift) {
        this.selectedShift = shift;
        txtShiftName.setText(shift.getShiftName());
        txtStartTime.setText(shift.getStartTime().toString());
        txtEndTime.setText(shift.getEndTime().toString());
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        resetStyles(); // Xóa viền đỏ cũ
        
        String newName = txtShiftName.getText().trim();
        String startStr = txtStartTime.getText().trim();
        String endStr = txtEndTime.getText().trim();

        // 1. Kiểm tra rỗng (Not Null)
        StringBuilder errorMsg = new StringBuilder();
        if (newName.isEmpty()) {
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
            showAlert(Alert.AlertType.WARNING, "Validation Error", errorMsg.toString());
            return;
        }

        try {
            // 2. Kiểm tra trùng tên (Ngoại trừ tên hiện tại của chính nó)
            // Nếu người dùng đổi sang một tên mới, ta mới check xem tên mới đó có ai dùng chưa
            if (!newName.equalsIgnoreCase(selectedShift.getShiftName())) {
                if (shiftDAO.isShiftNameExists(newName)) {
                    showAlert(Alert.AlertType.ERROR, "Duplicate Name", "The name '" + newName + "' is already taken.");
                    txtShiftName.setStyle("-fx-border-color: red;");
                    return;
                }
            }

            // 3. Parse giờ và kiểm tra logic
            LocalTime startTime = LocalTime.parse(startStr);
            LocalTime endTime = LocalTime.parse(endStr);

            if (!startTime.isBefore(endTime)) {
                showAlert(Alert.AlertType.WARNING, "Invalid Schedule", "Start time must be earlier than end time.");
                return;
            }

            // 4. Cập nhật đối tượng
            selectedShift.setShiftName(newName);
            selectedShift.setStartTime(startTime);
            selectedShift.setEndTime(endTime);

            if (shiftDAO.updateShift(selectedShift)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Shift updated successfully!");
                onClose(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update shift in database.");
            }

        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Format", "Please use HH:mm format (e.g. 09:00).");
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Shift: " + selectedShift.getShiftName());
        confirm.setContentText("Are you sure? This action cannot be undone.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (shiftDAO.deleteShift(selectedShift.getShiftID())) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Shift has been removed.");
                onClose(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete shift. It might be assigned to employees.");
            }
        }
    }

    @FXML
    private void onClose(ActionEvent event) {
        ((Stage) txtShiftName.getScene().getWindow()).close();
    }

    private void resetStyles() {
        txtShiftName.setStyle(null);
        txtStartTime.setStyle(null);
        txtEndTime.setStyle(null);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}