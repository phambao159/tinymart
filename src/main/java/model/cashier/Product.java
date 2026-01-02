package model.cashier;

public class Product {
    private String id;
    private String name;
    private double price;
    private String imagePath;
    private int categoryId;
    private int totalStock;

    public Product(String id, String name, double price, String imagePath, int categoryId, int totalStock) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.imagePath = imagePath;
    this.categoryId = categoryId;
    this.totalStock = totalStock;
}

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
    public int getCategoryId() { return categoryId; }
    public int getTotalStock() { return totalStock; }
    @Override
    public String toString() {
        return name;
    }
}