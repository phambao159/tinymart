package model.manager.supplier;

import java.time.LocalDateTime;

/**
 * Model class for Import (Phiếu nhập hàng) Cập nhật theo cấu trúc bảng Import
 * trong database tinymart
 */
public class Import {

    private int importID;       // ImportID int(10) UNSIGNED
    private int supplierID;     // SupplierID int(11)
    private LocalDateTime receiptDate; // ReceiptDate date (trong SQL là date, dùng LocalDateTime để đồng bộ code)
    private int employeeID;     // EmployeeID int(11)
    private double totalCost;   // TotalCost decimal(8,2)
    private String status;        // Note (Cột này không có trong SQL cũ nhưng thường dùng cho ghi chú, có thể giữ lại hoặc bỏ)

    // Constructor mặc định
    public Import() {
    }

    // Constructor đầy đủ (dùng khi lấy dữ liệu từ database)
    public Import(int importID, int supplierID, LocalDateTime receiptDate, int employeeID, double totalCost, String status) {
        this.importID = importID;
        this.supplierID = supplierID;
        this.receiptDate = receiptDate;
        this.employeeID = employeeID;
        this.totalCost = totalCost;
        this.status = status;
    }

    // Constructor không có ID (dùng khi tạo phiếu nhập mới để lưu vào DB)
    public Import(int supplierID, LocalDateTime receiptDate, int employeeID, double totalCost, String note) {
        this.supplierID = supplierID;
        this.receiptDate = receiptDate;
        this.employeeID = employeeID;
        this.totalCost = totalCost;
        this.status = status;
    }

    // Getter và Setter
    public int getImportID() {
        return importID;
    }

    public void setImportID(int importID) {
        this.importID = importID;
    }

    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public LocalDateTime getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDateTime receiptDate) {
        this.receiptDate = receiptDate;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Import{"
                + "importID=" + importID
                + ", supplierID=" + supplierID
                + ", employeeID=" + employeeID
                + ", receiptDate=" + receiptDate
                + ", totalCost=" + totalCost
                + '}';
    }
}
