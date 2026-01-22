package model.cashier;

public class CartItem {

    private String productId;
    private int sizeId;
    private String productName;
    private int quantity;
    private double price;
    private String promotionType;
    private double promotionValue;
    private Double total;

    private double sellingPrice;

    public CartItem(String productId, int sizeId, String productName, int quantity, double price, String promotionType, double promotionValue) {
        this.productId = productId;
        this.sizeId = sizeId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.promotionType = promotionType;
        this.promotionValue = promotionValue;
        this.total = price * quantity;
        this.sellingPrice = price;
    }

    public CartItem(String productId, int sizeId, String productName, int quantity, double price, String promotionType, double promotionValue, double sellingPrice) {
        this(productId, sizeId, productName, quantity, price, promotionType, promotionValue);
        this.sellingPrice = sellingPrice;
    }

    public String getProductId() {
        return productId;
    }

    public int getSizeId() {
        return sizeId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.total = this.price * this.quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public double getPromotionValue() {
        return promotionValue;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
}
