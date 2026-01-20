package model.manager.product;

/**
 * Lớp Model đại diện cho thông tin sản phẩm (Product)
 * Đã loại bỏ promotionID vì thuộc tính này đã chuyển sang bảng ProductSize.
 */
public class Product {

    private int productID;
    private String name;
    private int categoryID;
    private String unit;
    private String status;
    private String image;

    // Constructor mặc định
    public Product() {
    }

    // Constructor dùng khi thêm mới (không cần productID vì thường là tự tăng)
    public Product(String name, int categoryID, String unit, String status, String image) {
        this.name = name;
        this.categoryID = categoryID;
        this.unit = unit;
        this.status = status;
        this.image = image;
    }

    // Constructor dùng khi lấy dữ liệu từ DB (có đầy đủ productID)
    public Product(int productID, String name, int categoryID, String unit, String status, String image) {
        this.productID = productID;
        this.name = name;
        this.categoryID = categoryID;
        this.unit = unit;
        this.status = status;
        this.image = image;
    }

    // Getter và Setter
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