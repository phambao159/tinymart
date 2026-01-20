package model.manager.product;

/**
 * Lớp Model đại diện cho thông tin kích thước sản phẩm (ProductSize) Đã thêm
 * promotionID do thuộc tính này được chuyển từ Product sang đây.
 */
public class ProductSize {

    private int productSizeID;
    private int productID;
    private int sizeID;
    private int promotionID; // Cột mới thêm từ database
    private String productName;
    private String sizeType;
    private double costPrice;
    private double sellingPrice;
    private int stockQuantity;

    // Constructor mặc định
    public ProductSize() {
    }

    // Constructor dùng khi thêm mới (không cần productSizeID)
    public ProductSize(int productID, int sizeID, int promotionID, String sizeType,
            double costPrice, double sellingPrice, int stockQuantity) {
        this.productID = productID;
        this.sizeID = sizeID;
        this.promotionID = promotionID;
        this.sizeType = sizeType;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
    }

    // Constructor đầy đủ dùng khi lấy dữ liệu từ DB
    public ProductSize(int productSizeID, int productID, int sizeID, int promotionID,
            String sizeType, double costPrice, double sellingPrice, int stockQuantity) {
        this.productSizeID = productSizeID;
        this.productID = productID;
        this.sizeID = sizeID;
        this.promotionID = promotionID;
        this.sizeType = sizeType;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
    }

    // --- Getter và Setter ---
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductSizeID() {
        return productSizeID;
    }

    public void setProductSizeID(int productSizeID) {
        this.productSizeID = productSizeID;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getSizeID() {
        return sizeID;
    }

    public void setSizeID(int sizeID) {
        this.sizeID = sizeID;
    }

    public int getPromotionID() {
        return promotionID;
    }

    public void setPromotionID(int promotionID) {
        this.promotionID = promotionID;
    }

    public String getSizeType() {
        return sizeType;
    }

    public void setSizeType(String sizeType) {
        this.sizeType = sizeType;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

}
