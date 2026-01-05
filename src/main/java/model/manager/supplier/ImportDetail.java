package model.manager.supplier;

import java.time.LocalDate;

/**
 * Model class cho bảng ImportDetail (Chi tiết phiếu nhập hàng)
 */
public class ImportDetail {

    private int productID;
    private int importDetailID;   // ImportDetailID int (Primary Key)
    private int importID;         // ImportID int (Foreign Key)
    private int productSizeID;    // ProductSizeID int (Foreign Key)
    private long quantity;        // Quantity bigint
    private double importPrice;   // ImportPrice decimal(15,2)
    private LocalDate expiryDate; // ExpiryDate date
    private String productName;
    private String sizeName;

    // Constructor mặc định
    public ImportDetail() {
    }

    // Constructor đầy đủ (Dùng khi lấy dữ liệu từ database)
    public ImportDetail(int importDetailID, int importID, int productSizeID, long quantity, double importPrice, LocalDate expiryDate) {
        this.importDetailID = importDetailID;
        this.importID = importID;
        this.productSizeID = productSizeID;
        this.quantity = quantity;
        this.importPrice = importPrice;
        this.expiryDate = expiryDate;
    }

    // Constructor không có ID (Dùng khi tạo mới chi tiết để lưu vào DB)
    public ImportDetail(int importID, int productSizeID, long quantity, double importPrice, LocalDate expiryDate) {
        this.importID = importID;
        this.productSizeID = productSizeID;
        this.quantity = quantity;
        this.importPrice = importPrice;
        this.expiryDate = expiryDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    // Getter và Setter
    public int getImportDetailID() {
        return importDetailID;
    }

    public void setImportDetailID(int importDetailID) {
        this.importDetailID = importDetailID;
    }

    public int getImportID() {
        return importID;
    }

    public void setImportID(int importID) {
        this.importID = importID;
    }

    public int getProductSizeID() {
        return productSizeID;
    }

    public void setProductSizeID(int productSizeID) {
        this.productSizeID = productSizeID;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public double getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(double importPrice) {
        this.importPrice = importPrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }
    

    @Override
    public String toString() {
        return "ImportDetail{"
                + "importDetailID=" + importDetailID
                + ", productSizeID=" + productSizeID
                + ", quantity=" + quantity
                + ", importPrice=" + importPrice
                + ", expiryDate=" + expiryDate
                + '}';
    }
}
