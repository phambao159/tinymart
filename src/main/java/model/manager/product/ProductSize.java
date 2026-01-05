package model.manager.product;

public class ProductSize {

    private int productSizeID; // Khóa chính (Auto Increment trong DB)
    private int productID;     // Khóa ngoại trỏ đến bảng Product
    private int sizeID;        // Khóa ngoại trỏ đến bảng Size

    private String sizeType;    // Tên size để hiển thị (S, M, L...)
    private double costPrice;
    private double sellingPrice;
    private int stockQuantity;

    public ProductSize() {
    }

    // Constructor dùng khi lấy dữ liệu từ DB lên (Có đủ ID)
    public ProductSize(int productSizeID, int productID, int sizeID, String sizeType, double costPrice, double sellingPrice, int stockQuantity) {
        this.productSizeID = productSizeID;
        this.productID = productID;
        this.sizeID = sizeID;
        this.sizeType = sizeType;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
    }
 

    // Getter và Setter
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
