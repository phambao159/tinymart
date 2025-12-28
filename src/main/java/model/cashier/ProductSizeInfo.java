package model.cashier;

public class ProductSizeInfo {
    private int sizeId;
    private String sizeName;
    private double price;

    public ProductSizeInfo(int sizeId, String sizeName, double price) {
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.price = price;
    }

    public String getSizeName() { return sizeName; }
    public double getPrice() { return price; }
    
    @Override
    public String toString() {
        return sizeName;
    }
}