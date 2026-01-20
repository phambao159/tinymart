package model.cashier;

public class CartItem {

    private String productId;
    private int sizeId;
    private String productName;
    private int quantity;
    private double price;
    private double total;
    private String promotionType;
    private double promotionValue;

    public CartItem(String productId, int sizeId, String productName, int quantity, double price, String promotionType, double promotionValue) {
        this.productId = productId;
        this.sizeId = sizeId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.total = price * quantity;
        this.promotionType = promotionType;
        this.promotionValue = promotionValue;
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

    public String getPromotionType() {
        return promotionType;
    }

    public double getPromotionValue() {
        return promotionValue;
    }
}
