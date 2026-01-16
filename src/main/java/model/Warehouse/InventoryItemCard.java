package model.Warehouse;

public class InventoryItemCard {
    private String productName;
    private String sizeType;
    private String expiryDate;
    private int shelfQuantity;
    private String status;
    private String imagePath;

    // Constructor
    public InventoryItemCard() {}

    public InventoryItemCard(String productName, String sizeType,
                             String expiryDate, int shelfQuantity,
                             String status, String imagePath) {
        this.productName = productName;
        this.sizeType = sizeType;
        this.expiryDate = expiryDate;
        this.shelfQuantity = shelfQuantity;
        this.status = status;
        this.imagePath = imagePath;
    }

    // Getter & Setter
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