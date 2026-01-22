package model.Warehouse;

import java.sql.Date;
import java.sql.Time;
import java.math.BigDecimal;

public class EmployeeShift {
    private int employeeShiftID;
    private int employeeID;
    private int shiftID;
    private Date workDate;
    private Time checkInTime;
    private Time checkOutTime;
    private BigDecimal startCash;
    private BigDecimal totalSales;
    private BigDecimal endCash;

    // Constructors
    public EmployeeShift() {}

    public EmployeeShift(int employeeShiftID, int employeeID, int shiftID, Date workDate,
                         Time checkInTime, Time checkOutTime,
                         BigDecimal startCash, BigDecimal totalSales, BigDecimal endCash) {
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

    // Getters & Setters
    public int getEmployeeShiftID() { return employeeShiftID; }
    public void setEmployeeShiftID(int employeeShiftID) { this.employeeShiftID = employeeShiftID; }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public int getShiftID() { return shiftID; }
    public void setShiftID(int shiftID) { this.shiftID = shiftID; }

    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }

    public Time getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Time checkInTime) { this.checkInTime = checkInTime; }

    public Time getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Time checkOutTime) { this.checkOutTime = checkOutTime; }

    public BigDecimal getStartCash() { return startCash; }
    public void setStartCash(BigDecimal startCash) { this.startCash = startCash; }

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public BigDecimal getEndCash() { return endCash; }
    public void setEndCash(BigDecimal endCash) { this.endCash = endCash; }
}