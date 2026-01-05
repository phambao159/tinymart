package controller.manager.employee;

import dao.manager.employee.EmployeeDAO;
import dao.manager.employee.EmployeeShiftDAO;
import dao.manager.employee.ShiftDAO;
import model.manager.employee.Employee;
import model.manager.employee.EmployeeShift;
import model.manager.employee.Shift;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class EditEmployeeShiftController implements Initializable {

    @FXML private ComboBox<Employee> cbEmployee;
    @FXML private ComboBox<Shift> cbShift;
    @FXML private DatePicker dpWorkDate;
    @FXML private TextField txtCheckIn;
    @FXML private TextField txtCheckOut;
    @FXML private TextField txtStartCash;
    @FXML private TextField txtEndCash;
    @FXML private TextField txtTotalSales;

    private final EmployeeShiftDAO esDAO = new EmployeeShiftDAO();
    private final EmployeeDAO empDAO = new EmployeeDAO();
    private final ShiftDAO shiftDAO = new ShiftDAO();
    
    private EmployeeShift currentAssignment;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
    }

    /**
     * Cấu hình hiển thị cho ComboBox và load dữ liệu danh mục
     */
    private void setupComboBoxes() {
        // Load danh sách nhân viên và ca trực
        cbEmployee.setItems(FXCollections.observableArrayList(empDAO.getData()));
        cbShift.setItems(FXCollections.observableArrayList(shiftDAO.getAllShifts()));

        // Hiển thị tên thay vì địa chỉ vùng nhớ
        cbEmployee.setConverter(new StringConverter<Employee>() {
            @Override public String toString(Employee e) { return e == null ? "" : e.getFullName(); }
            @Override public Employee fromString(String s) { return null; }
        });

        cbShift.setConverter(new StringConverter<Shift>() {
            @Override public String toString(Shift s) { return s == null ? "" : s.getShiftName(); }
            @Override public Shift fromString(String s) { return null; }
        });
    }

    /**
     * Nhận dữ liệu từ màn hình danh sách truyền sang
     */
    public void initData(EmployeeShift es) {
        this.currentAssignment = es;

        // Điền dữ liệu vào các trường
        dpWorkDate.setValue(es.getWorkDate());
        txtStartCash.setText(es.getStartCash().toString());
        txtEndCash.setText(es.getEndCash().toString());
        txtTotalSales.setText(es.getTotalSales().toString());
        
        if (es.getCheckInTime() != null) txtCheckIn.setText(es.getCheckInTime().toString());
        if (es.getCheckOutTime() != null) txtCheckOut.setText(es.getCheckOutTime().toString());

        // Chọn đúng giá trị trong ComboBox dựa trên ID
        cbEmployee.getItems().stream()
            .filter(e -> e.getEmployeeID() == es.getEmployeeID())
            .findFirst().ifPresent(cbEmployee::setValue);
            
        cbShift.getItems().stream()
            .filter(s -> s.getShiftID() == es.getShiftID())
            .findFirst().ifPresent(cbShift::setValue);
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        try {
            // Cập nhật dữ liệu từ UI vào object hiện tại
            currentAssignment.setEmployeeID(cbEmployee.getValue().getEmployeeID());
            currentAssignment.setShiftID(cbShift.getValue().getShiftID());
            currentAssignment.setWorkDate(dpWorkDate.getValue());
            currentAssignment.setStartCash(new BigDecimal(txtStartCash.getText()));
            currentAssignment.setEndCash(new BigDecimal(txtEndCash.getText()));
            currentAssignment.setTotalSales(new BigDecimal(txtTotalSales.getText()));

            // Parse thời gian (phải nhập đúng định dạng HH:mm)
            if (!txtCheckIn.getText().isEmpty()) 
                currentAssignment.setCheckInTime(LocalTime.parse(txtCheckIn.getText()));
            if (!txtCheckOut.getText().isEmpty()) 
                currentAssignment.setCheckOutTime(LocalTime.parse(txtCheckOut.getText()));

            if (esDAO.updateAssignment(currentAssignment)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Assignment updated successfully!");
                onClose(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update database.");
            }
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Time", "Please use format HH:mm (e.g., 08:30).");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Cash", "Cash values must be numbers.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }


    @FXML
    private void onClose(ActionEvent event) {
        ((Stage) txtCheckIn.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}