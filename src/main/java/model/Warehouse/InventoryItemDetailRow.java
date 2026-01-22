package model.Warehouse;

public class InventoryItemDetailRow {
    private String expiryDate;
    private int quantity;
    private int shelfQuantity;

    public InventoryItemDetailRow(String expiryDate, int quantity, int shelfQuantity) {
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.shelfQuantity = shelfQuantity;
    }

    public String getExpiryDate() { return expiryDate; }
    public int getQuantity() { return quantity; }
    public int getShelfQuantity() { return shelfQuantity; }
}