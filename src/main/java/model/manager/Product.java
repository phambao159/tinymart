package model.manager;

import java.math.BigDecimal;

public class Product {

    private int productID;
    private String name;

    private int promotionID;
    private int categoryID;
    private String unit;

    private String status;
    private String image;


    public Product(String name, int promotionID, int categoryID, String unit, String status, String image) {
        this.name = name;
        this.promotionID = promotionID;
        this.categoryID = categoryID;
        this.unit = unit;
        this.status = status;
        this.image = image;
    }


    public Product(int productID, String name, int promotionID, int categoryID, String unit, String status, String image) {
        this.productID = productID;
        this.name = name;
        this.promotionID = promotionID;
        this.categoryID = categoryID;
        this.unit = unit;
        this.status = status;
        this.image = image;
    }


    public Product() {
    }



    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public int getPromotionID() {
        return promotionID;
    }

    public void setPromotionID(int promotionID) {
        this.promotionID = promotionID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}