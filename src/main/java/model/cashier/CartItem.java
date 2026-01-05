package model.cashier;

public class CartItem {

    private String productId;
    private int sizeId;
    private String productName;
    private int quantity;
    private double price;
    private double total;

    public CartItem(String productId, int sizeId, String productName, int quantity, double price) {
        this.productId = productId;
        this.sizeId = sizeId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.total = price * quantity;
    }

    public int getSizeId() {
        return sizeId;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return total;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.total = this.price * this.quantity;
    }
}
