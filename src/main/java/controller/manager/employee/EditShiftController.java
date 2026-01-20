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
    public void initialize(URL url, ResourceBundle rb) {
    }

    // Method to receive data from the main TableView
    public void initData(Shift shift) {
        this.selectedShift = shift;
        txtShiftName.setText(shift.getShiftName());
        txtStartTime.setText(shift.getStartTime().toString());
        txtEndTime.setText(shift.getEndTime().toString());
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        try {
            selectedShift.setShiftName(txtShiftName.getText().trim());
            selectedShift.setStartTime(LocalTime.parse(txtStartTime.getText().trim()));
            selectedShift.setEndTime(LocalTime.parse(txtEndTime.getText().trim()));

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
        confirm.setContentText("Are you sure you want to delete this shift?");
        
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}