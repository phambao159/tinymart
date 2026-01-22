package model.Warehouse;

public class InventoryItemCard {
    private int productSizeId;
    private String productName;
    private String sizeType;
    private int stock;              // tổng Quantity + ShelfQuantity
    private String imageFileName;

    public InventoryItemCard(int productSizeId, String productName, String sizeType,
                             int stock, String imageFileName) {
        this.productSizeId = productSizeId;
        this.productName = productName;
        this.sizeType = sizeType;
        this.stock = stock;
        this.imageFileName = imageFileName;
    }

    public int getProductSizeId() { return productSizeId; }
    public String getProductName() { return productName; }
    public String getSizeType() { return sizeType; }
    public int getStock() { return stock; }

    public String getImagePath() {
        if (imageFileName == null || imageFileName.isEmpty()) return null;
        return "/image/manager/" + imageFileName;
    }
}