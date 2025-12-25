package model.manager;

import java.time.LocalDate;

public class EmployeeShift {
    private int employeeID;
    private int shiftID;
    private LocalDate date;
    
    // Các trường bổ sung để hiển thị lên TableView (nếu cần)
    private String employeeName;
    private String shiftName;

    public EmployeeShift() {
    }

    // Constructor dùng để lưu xuống DB
    public EmployeeShift(int employeeID, int shiftID, LocalDate date) {
        this.employeeID = employeeID;
        this.shiftID = shiftID;
        this.date = date;
    }

    // Getter và Setter
    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public int getShiftID() { return shiftID; }
    public void setShiftID(int shiftID) { this.shiftID = shiftID; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
}