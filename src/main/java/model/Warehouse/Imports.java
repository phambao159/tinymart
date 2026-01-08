package model.Warehouse;

import java.time.LocalDate;

public class Imports {
    private int importID;
    private int supplierID;
    private int employeeID;
    private LocalDate receiptDate;
    private double totalCost;
    private String status;

    // Thêm field để hiển thị tên
    private String supplierName;
    private String employeeName;

    // Getter/Setter
    public int getImportID() { return importID; }
    public void setImportID(int importID) { this.importID = importID; }

    public int getSupplierID() { return supplierID; }
    public void setSupplierID(int supplierID) { this.supplierID = supplierID; }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
}