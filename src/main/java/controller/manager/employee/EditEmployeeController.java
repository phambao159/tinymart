package controller.manager.employee;

import dao.manager.employee.EmployeeDAO;
import dao.manager.employee.EmployeeShiftDAO;
import model.manager.employee.Employee;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditEmployeeController implements Initializable {

    @FXML
    private TextField txtName, txtPhone, txtAddress, txtSalary, txtUser;
    @FXML
    private TextField txtPassword;
    @FXML
    private DatePicker dpDob, dpHireDate;
    @FXML
    private ComboBox<String> cbRole;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private Employee currentEmployee;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbRole.setItems(FXCollections.observableArrayList("Manager", "Staff", "Warehouse"));
    }

    public void initData(Employee emp) {
        this.currentEmployee = emp;
        txtName.setText(emp.getFullName());
        dpDob.setValue(emp.getDateOfBirth());
        txtPhone.setText(emp.getPhoneNumber());
        txtAddress.setText(emp.getAddress());
        cbRole.setValue(emp.getRole());
        txtSalary.setText(String.valueOf(emp.getBaseSalary()));
        txtUser.setText(emp.getUser());
        txtPassword.setText(emp.getPassword());
        dpHireDate.setValue(emp.getHireDate());
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        try {
            currentEmployee.setFullName(txtName.getText().trim());
            currentEmployee.setDateOfBirth(dpDob.getValue());
            currentEmployee.setPhoneNumber(txtPhone.getText().trim());
            currentEmployee.setAddress(txtAddress.getText().trim());
            currentEmployee.setRole(cbRole.getValue());
            currentEmployee.setBaseSalary(Long.parseLong(txtSalary.getText().trim()));
            currentEmployee.setUser(txtUser.getText().trim());
            currentEmployee.setPassword(txtPassword.getText());
            currentEmployee.setHireDate(dpHireDate.getValue());

            if (employeeDAO.update(currentEmployee)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee information updated successfully!");
                onClose(event);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid data: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteInsideEdit(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this employee?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (employeeDAO.delete(currentEmployee.getEmployeeID())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee deleted successfully.");
                onClose(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete this employee. It may be linked to other data.");
            }
        }
    }

    @FXML
    private void onClose(ActionEvent event) {
        ((Stage) txtName.getScene().getWindow()).close();
    }

    @FXML
    private void onCalculateSalary(ActionEvent event) {
        try {
            LocalDate today = LocalDate.now();
            int employeeId = currentEmployee.getEmployeeID();
            double baseSalary = Double.parseDouble(txtSalary.getText());

            // Định dạng để lấy "Tháng Năm" (ví dụ: December 2025)
            DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

            // --- 1. Tính toán cho Tháng Trước (T-1) ---
            LocalDate firstOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
            LocalDate lastOfLastMonth = firstOfLastMonth.withDayOfMonth(firstOfLastMonth.lengthOfMonth());
            String lastMonthName = firstOfLastMonth.format(monthYearFormatter);

            EmployeeShiftDAO esDAO = new EmployeeShiftDAO();
            long daysWorkedLastMonth = esDAO.getAllAssignments().stream()
                    .filter(es -> es.getEmployeeID() == employeeId)
                    .filter(es -> !es.getWorkDate().isBefore(firstOfLastMonth) && !es.getWorkDate().isAfter(lastOfLastMonth))
                    .filter(es -> es.getCheckOutTime() != null)
                    .map(es -> es.getWorkDate())
                    .distinct().count();

            double salaryLastMonth = calculateLogic(baseSalary, daysWorkedLastMonth);

            // --- 2. Tính toán cho Tháng Hiện Tại (T) ---
            String currentMonthName = today.format(monthYearFormatter);
            String currentMonthInfo;

            // Logic: Nếu chưa qua ngày 10 của tháng sau thì chưa tính lương tháng hiện tại
            // Ví dụ: Hôm nay là 31/12/2025, tháng sau là tháng 1. Ngày chốt là 10/01/2026.
            LocalDate salaryReleaseDate = today.plusMonths(1).withDayOfMonth(10);

            if (today.isBefore(salaryReleaseDate)) {
                currentMonthInfo = String.format("%s : Not available (Pending until 10/%02d/%d)",
                        currentMonthName,
                        salaryReleaseDate.getMonthValue(),
                        salaryReleaseDate.getYear());
            } else {
                LocalDate firstOfThisMonth = today.withDayOfMonth(1);
                long daysWorkedThisMonth = esDAO.getAllAssignments().stream()
                        .filter(es -> es.getEmployeeID() == employeeId)
                        .filter(es -> !es.getWorkDate().isBefore(firstOfThisMonth) && !es.getWorkDate().isAfter(today))
                        .filter(es -> es.getCheckOutTime() != null)
                        .map(es -> es.getWorkDate())
                        .distinct().count();

                double salaryThisMonth = calculateLogic(baseSalary, daysWorkedThisMonth);
                currentMonthInfo = String.format("%s : %,.0f (%d work day)",
                        currentMonthName, salaryThisMonth, daysWorkedThisMonth);
            }

            // --- 3. Hiển thị Alert ---
            String content = String.format(
                    "Employee: %s\n\n"
                    + "%s : %,.0f (%d work day)\n"
                    + "%s",
                    txtName.getText(),
                    lastMonthName, salaryLastMonth, daysWorkedLastMonth,
                    currentMonthInfo
            );

            showAlert(Alert.AlertType.INFORMATION, "Salary Details", content);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not calculate salary: " + e.getMessage());
        }
    }

    /**
     * Logic tính lương: - Đủ >= 26 ngày: Full lương cơ bản. - Dưới 26 ngày: Lấy
     * (Lương cơ bản) - (Số ngày nghỉ * Lương 1 ngày). - Lương 1 ngày = Lương cơ
     * bản / 30.
     */
    private double calculateLogic(double baseSalary, long daysWorked) {
        if (daysWorked >= 26) {
            return baseSalary;
        } else {
            double salaryPerDay = baseSalary / 30;
            long daysOff = 26 - daysWorked; // Tính số ngày bị thiếu so với mốc 26
            // Cách tính trừ lương theo yêu cầu của bạn:
            // Lương = Lương CB - (Số ngày nghỉ * Lương 1 ngày)
            // Lưu ý: Ở đây tính dựa trên 30 ngày để lấy đơn giá ngày công
            double penalty = (30 - daysWorked) * salaryPerDay;
            double finalSalary = baseSalary - penalty;
            return Math.max(0, finalSalary); // Tránh lương âm
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
