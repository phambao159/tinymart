package model.manager.employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class EmployeeShift {

    private int employeeShiftID; // Primary Key from SQL
    private int employeeID;
    private int shiftID;
    private LocalDate workDate; // Renamed from 'date' to match 'WorkDate' in SQL
    private LocalTime checkInTime; // From SQL
    private LocalTime checkOutTime; // From SQL
    private BigDecimal startCash; // From SQL
    private BigDecimal totalSales; // From SQL
    private BigDecimal endCash; // From SQL

    // Display purposes for TableView
    private String employeeName;
    private String shiftName;

    public EmployeeShift() {
    }

    // Full constructor based on Database structure
    public EmployeeShift(int employeeShiftID, int employeeID, int shiftID, LocalDate workDate,
            LocalTime checkInTime, LocalTime checkOutTime, BigDecimal startCash,
            BigDecimal totalSales, BigDecimal endCash) {
        this.employeeShiftID = employeeShiftID;
        this.employeeID = employeeID;
        this.shiftID = shiftID;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.startCash = startCash;
        this.totalSales = totalSales;
        this.endCash = endCash;
    }

    // Getters and Setters
    public int getEmployeeShiftID() {
        return employeeShiftID;
    }

    public void setEmployeeShiftID(int employeeShiftID) {
        this.employeeShiftID = employeeShiftID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public int getShiftID() {
        return shiftID;
    }

    public void setShiftID(int shiftID) {
        this.shiftID = shiftID;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public BigDecimal getStartCash() {
        return startCash;
    }

    public void setStartCash(BigDecimal startCash) {
        this.startCash = startCash;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getEndCash() {
        return endCash;
    }

    public void setEndCash(BigDecimal endCash) {
        this.endCash = endCash;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }
}
