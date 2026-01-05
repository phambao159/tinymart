package controller.manager.employee;

import dao.manager.employee.EmployeeDAO;
import dao.manager.employee.EmployeeShiftDAO;
import dao.manager.employee.ShiftDAO;
import model.manager.employee.Employee;
import model.manager.employee.EmployeeShift;
import model.manager.employee.Shift;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class AddEmployeeShiftController implements Initializable {

    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private ComboBox<Shift> cbShift;
    @FXML private DatePicker dpWorkDate;
    @FXML private TextField txtStartCash;

    private EmployeeDAO empDAO = new EmployeeDAO();
    private ShiftDAO shiftDAO = new ShiftDAO();
    private EmployeeShiftDAO assignmentDAO = new EmployeeShiftDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadComboBoxes();
        dpWorkDate.setValue(LocalDate.now()); // Mặc định là ngày hôm nay
    }

    private void loadComboBoxes() {
        // Load danh sách nhân viên
        cbEmployee.setItems(FXCollections.observableArrayList(empDAO.getData()));
        cbEmployee.setConverter(new StringConverter<Employee>() {
            @Override public String toString(Employee object) {
                return object == null ? "" : object.getFullName() + " (ID: " + object.getEmployeeID() + ")";
            }
            @Override public Employee fromString(String string) { return null; }
        });

        // Load danh sách ca trực
        cbShift.setItems(FXCollections.observableArrayList(shiftDAO.getAllShifts()));
        cbShift.setConverter(new StringConverter<Shift>() {
            @Override public String toString(Shift object) {
                return object == null ? "" : object.getShiftName() + " (" + object.getStartTime() + " - " + object.getEndTime() + ")";
            }
            @Override public Shift fromString(String string) { return null; }
        });
    }

    @FXML
    private void onSave(ActionEvent event) {
        Employee selectedEmp = cbEmployee.getValue();
        Shift selectedShift = cbShift.getValue();
        LocalDate workDate = dpWorkDate.getValue();
        String cashText = txtStartCash.getText().trim();

        if (selectedEmp == null || selectedShift == null || workDate == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select Employee, Shift and Date.");
            return;
        }

        try {
            EmployeeShift es = new EmployeeShift();
            es.setEmployeeID(selectedEmp.getEmployeeID());
            es.setShiftID(selectedShift.getShiftID());
            es.setWorkDate(workDate);
            es.setStartCash(new BigDecimal(cashText.isEmpty() ? "0" : cashText));
            es.setTotalSales(BigDecimal.ZERO);
            es.setEndCash(BigDecimal.ZERO);

            if (assignmentDAO.addAssignment(es)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Shift assigned successfully!");
                onClose(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign shift. This employee might already be assigned to this shift on this date.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Initial cash must be a valid number.");
        }
    }

    @FXML
    private void onClose(ActionEvent event) {
        ((Stage) cbEmployee.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}