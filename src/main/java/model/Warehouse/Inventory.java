package model.Warehouse;

public class Inventory {
    private int inventoryId;
    private int productSizeId;
    private String productName;
    private String sizeType;
    private String expiryDate;
    private int shelfQuantity;
    private String status;
    private String imagePath; // đường dẫn ảnh từ bảng Product

    // Constructor
    public Inventory() {}

    public Inventory(int inventoryId, int productSizeId, String productName,
                     String sizeType, String expiryDate, int shelfQuantity,
                     String status, String imagePath) {
        this.inventoryId = inventoryId;
        this.productSizeId = productSizeId;
        this.productName = productName;
        this.sizeType = sizeType;
        this.expiryDate = expiryDate;
        this.shelfQuantity = shelfQuantity;
        this.status = status;
        this.imagePath = imagePath;
    }

    // Getter & Setter
    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }

    public int getProductSizeId() { return productSizeId; }
    public void setProductSizeId(int productSizeId) { this.productSizeId = productSizeId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSizeType() { return sizeType; }
    public void setSizeType(String sizeType) { this.sizeType = sizeType; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public int getShelfQuantity() { return shelfQuantity; }
    public void setShelfQuantity(int shelfQuantity) { this.shelfQuantity = shelfQuantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}