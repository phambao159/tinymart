package model.Warehouse;

import javafx.scene.image.Image;

public class InventoryReport {
    private int id;
    private String productName;
    private String sizeType;
    private String expireDate;
    private String actionType;
    private int quantity;
    private String status;
    private Image productImage;
    private String updateTime;

    public InventoryReport(int id, String productName, String sizeType, String expireDate,
                           String actionType, int quantity, String status,
                           Image productImage, String updateTime) {
        this.id = id;
        this.productName = productName;
        this.sizeType = sizeType;
        this.expireDate = expireDate;
        this.actionType = actionType;
        this.quantity = quantity;
        this.status = status;
        this.productImage = productImage;
        this.updateTime = updateTime;
    }

    // Getter
    public Image getProductImage() { return productImage; }
    public String getProductName() { return productName; }
    public String getSizeType() { return sizeType; }
    public String getExpireDate() { return expireDate; }
    public String getActionType() { return actionType; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public String getUpdateTime() { return updateTime; }
}