package model.cashier;

public class ProductSizeInfo {

    private int sizeId;
    private String sizeName;
    private double price;
    private int stock;

    public ProductSizeInfo(int sizeId, String sizeName, double price, int stock) {
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.price = price;
        this.stock = stock;
    }

    public String getSizeName() {
        return sizeName;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public int getSizeId() {
        return sizeId;
    }

    @Override
    public String toString() {
        return sizeName;
    }
}
