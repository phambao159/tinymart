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

    @FXML private TextField txtShiftName;
    @FXML private TextField txtStartTime; // User enters HH:mm
    @FXML private TextField txtEndTime;   // User enters HH:mm

    private ShiftDAO shiftDAO = new ShiftDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // You could set default values here if desired
    }

    @FXML
    private void onSave(ActionEvent event) {
        String name = txtShiftName.getText().trim();
        String startStr = txtStartTime.getText().trim();
        String endStr = txtEndTime.getText().trim();

        // 1. Check for empty fields
        if (name.isEmpty() || startStr.isEmpty() || endStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all fields.");
            return;
        }

        try {
            // 2. Parse Strings to LocalTime
            // Supports formats like 08:30 or 08:30:00
            LocalTime startTime = LocalTime.parse(startStr);
            LocalTime endTime = LocalTime.parse(endStr);

            // 3. Logic check: Start time should be before End time (optional)
            if (!startTime.isBefore(endTime)) {
                showAlert(Alert.AlertType.WARNING, "Invalid Schedule", "Start time must be earlier than end time.");
                return;
            }

            // 4. Create Shift object using your constructor: Shift(startTime, endTime, shiftName)
            Shift newShift = new Shift(startTime, endTime, name);

            // 5. Save to Database
            if (shiftDAO.addShift(newShift)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "New shift created successfully!");
                onClose(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save the shift to the database.");
            }

        } catch (DateTimeParseException e) {
            // Error if user types "8am" or "random text" instead of "08:00"
            showAlert(Alert.AlertType.ERROR, "Invalid Time Format", "Please use HH:mm format (e.g., 08:30 or 14:00).");
        }
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