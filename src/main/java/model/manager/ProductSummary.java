package model.manager;


public class ProductSummary {


    private int productID;
    private String name;
    private int categoryID;
    private String unit;
    private String image;
    private double minSellingPrice;
    private long totalStockQuantity; 
    private String status;


    public ProductSummary(int productID, String name, int categoryID, String unit, String image, double minSellingPrice, long totalStockQuantity, String status) {
        this.productID = productID;
        this.name = name;
        this.categoryID = categoryID;
        this.unit = unit;
        this.image = image;
        this.minSellingPrice = minSellingPrice;
        this.totalStockQuantity = totalStockQuantity;
        this.status = status;
    }



    public ProductSummary() {
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getMinSellingPrice() {
        return minSellingPrice;
    }

    public void setMinSellingPrice(double minSellingPrice) {
        this.minSellingPrice = minSellingPrice;
    }

    public long getTotalStockQuantity() {
        return totalStockQuantity;
    }

    public void setTotalStockQuantity(long totalStockQuantity) {
        this.totalStockQuantity = totalStockQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
