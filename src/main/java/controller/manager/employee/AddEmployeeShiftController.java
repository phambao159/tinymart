package controller.manager.employee;

import dao.manager.employee.EmployeeDAO;
import dao.manager.employee.EmployeeShiftDAO;
import dao.manager.employee.ShiftDAO;
import model.manager.employee.Employee;
import model.manager.employee.EmployeeShift;
import model.manager.employee.Shift;
import java.math.BigDecimal;
import java.net.URL;
import java.time.DayOfWeek;
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
    @FXML private TextField txtStartCash;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    
    // CheckBoxes cho các thứ trong tuần
    @FXML private CheckBox chkMon;
    @FXML private CheckBox chkTue;
    @FXML private CheckBox chkWed;
    @FXML private CheckBox chkThu;
    @FXML private CheckBox chkFri;
    @FXML private CheckBox chkSat;
    @FXML private CheckBox chkSun;

    private EmployeeDAO empDAO = new EmployeeDAO();
    private ShiftDAO shiftDAO = new ShiftDAO();
    private EmployeeShiftDAO assignmentDAO = new EmployeeShiftDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadComboBoxes();
        // Mặc định chọn từ hôm nay đến hết tuần sau
        dpFromDate.setValue(LocalDate.now());
        dpToDate.setValue(LocalDate.now().plusDays(7));
    }

    private void loadComboBoxes() {
        cbEmployee.setItems(FXCollections.observableArrayList(empDAO.getData()));
        cbEmployee.setConverter(new StringConverter<Employee>() {
            @Override public String toString(Employee object) {
                return object == null ? "" : object.getFullName() + " (" + object.getRole() + ")";
            }
            @Override public Employee fromString(String string) { return null; }
        });

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
        LocalDate startDate = dpFromDate.getValue();
        LocalDate endDate = dpToDate.getValue();
        String cashText = txtStartCash.getText().trim();

        // 1. Kiểm tra đầu vào cơ bản
        if (selectedEmp == null || selectedShift == null || startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        if (endDate.isBefore(startDate)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "End Date cannot be before Start Date.");
            return;
        }

        try {
            BigDecimal startCash = new BigDecimal(cashText.isEmpty() ? "0" : cashText);
            int successCount = 0;
            int failureCount = 0;

            // 2. Vòng lặp duyệt qua từng ngày từ startDate đến endDate
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                
                // 3. Kiểm tra xem thứ của ngày hiện tại có được chọn không
                if (isDaySelected(current.getDayOfWeek())) {
                    EmployeeShift es = new EmployeeShift();
                    es.setEmployeeID(selectedEmp.getEmployeeID());
                    es.setShiftID(selectedShift.getShiftID());
                    es.setWorkDate(current);
                    es.setStartCash(startCash);
                    es.setTotalSales(BigDecimal.ZERO);
                    es.setEndCash(BigDecimal.ZERO);

                    if (assignmentDAO.addAssignment(es)) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                }
                current = current.plusDays(1); // Sang ngày tiếp theo
            }

            // 4. Thông báo kết quả tổng hợp
            if (successCount > 0) {
                String msg = "Successfully assigned " + successCount + " shifts.";
                if (failureCount > 0) {
                    msg += "\n(" + failureCount + " days failed due to duplication)";
                }
                showAlert(Alert.AlertType.INFORMATION, "Process Completed", msg);
                onClose(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Process Failed", "No shifts were created. Check if dates/days were selected correctly.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Initial cash must be a valid number.");
        }
    }

    /**
     * Kiểm tra xem Thứ tương ứng có được tích chọn trên giao diện không
     */
    private boolean isDaySelected(DayOfWeek day) {
        switch (day) {
            case MONDAY:    return chkMon.isSelected();
            case TUESDAY:   return chkTue.isSelected();
            case WEDNESDAY: return chkWed.isSelected();
            case THURSDAY:  return chkThu.isSelected();
            case FRIDAY:    return chkFri.isSelected();
            case SATURDAY:  return chkSat.isSelected();
            case SUNDAY:    return chkSun.isSelected();
            default:        return false;
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