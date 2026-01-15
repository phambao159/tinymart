package model.manager.supplier;

import java.time.LocalDate;

/**
 * Model class cho bảng ImportDetail (Chi tiết phiếu nhập hàng)
 * Đã bổ sung shelfQuantity và promotionName
 */
public class ImportDetail {

    private int importDetailID;   // Primary Key
    private int importID;         // Foreign Key từ bảng Import
    private int productID;        // ID của sản phẩm (dùng để truy vấn)
    private int productSizeID;    // Foreign Key từ bảng ProductSize
    private long quantity;        // Số lượng nhập kho (bigint)
    private int shelfQuantity;    // Số lượng trưng bày/bán trên kệ (Mới thêm)
    private double importPrice;   // Giá nhập (decimal)
    private LocalDate expiryDate; // Ngày hết hạn
    
    // Các trường bổ trợ dùng để hiển thị lên TableView (JOIN từ bảng khác)
    private String productName;
    private String sizeName;
    private String promotionName; 

    // 1. Constructor không đối số
    public ImportDetail() {
    }

    // 2. Constructor đầy đủ (Dùng khi lấy dữ liệu từ Database)
    public ImportDetail(int importDetailID, int importID, int productSizeID, long quantity, 
                        int shelfQuantity, double importPrice, LocalDate expiryDate) {
        this.importDetailID = importDetailID;
        this.importID = importID;
        this.productSizeID = productSizeID;
        this.quantity = quantity;
        this.shelfQuantity = shelfQuantity;
        this.importPrice = importPrice;
        this.expiryDate = expiryDate;
    }

    // 3. Constructor để tạo mới (Không cần ID tự tăng)
    public ImportDetail(int importID, int productSizeID, long quantity, 
                        int shelfQuantity, double importPrice, LocalDate expiryDate) {
        this.importID = importID;
        this.productSizeID = productSizeID;
        this.quantity = quantity;
        this.shelfQuantity = shelfQuantity;
        this.importPrice = importPrice;
        this.expiryDate = expiryDate;
    }

    // --- GETTER VÀ SETTER ---

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

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
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

    public int getShelfQuantity() {
        return shelfQuantity;
    }

    public void setShelfQuantity(int shelfQuantity) {
        this.shelfQuantity = shelfQuantity;
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

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    // --- TO STRING ---
    @Override
    public String toString() {
        return "ImportDetail{" +
                "importDetailID=" + importDetailID +
                ", productSizeID=" + productSizeID +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", shelfQuantity=" + shelfQuantity +
                ", importPrice=" + importPrice +
                ", expiryDate=" + expiryDate +
                '}';
    }
}