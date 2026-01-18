package model.cashier;

public class ProductSizeInfo {

    private int sizeId;
    private String sizeName;
    private double price;
    private int stock;
    private String promoType;
    private double promoValue;
    private String promoDescription;

    public ProductSizeInfo(int sizeId, String sizeName, double price, int stock, String promoType, double promoValue, String promoDescription) {
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.price = price;
        this.stock = stock;
        this.promoType = promoType;
        this.promoValue = promoValue;
        this.promoDescription = promoDescription;
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

    public String getPromoType() {
        return promoType;
    }

    public double getPromoValue() {
        return promoValue;
    }

    public String getPromoDescription() {
        return promoDescription;
    }
}
