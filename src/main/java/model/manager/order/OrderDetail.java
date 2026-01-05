package model.manager.order;

public class OrderDetail {

    private int orderDetailID;
    private int orderID;
    private int productSizeID;
    private int quantity;

    // Các trường mở rộng để hiển thị (Lấy qua JOIN)
    private String productName;
    private String typeName;
    private double priceAtCurrent; // Giá lấy từ bảng ProductSize lúc JOIN

    public OrderDetail() {
    }

    public OrderDetail(int orderDetailID, int orderID, int productSizeID, int quantity) {
        this.orderDetailID = orderDetailID;
        this.orderID = orderID;
        this.productSizeID = productSizeID;
        this.quantity = quantity;
    }

    // Getters and Setters ...
    public int getOrderDetailID() {
        return orderDetailID;
    }

    public void setOrderDetailID(int orderDetailID) {
        this.orderDetailID = orderDetailID;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getProductSizeID() {
        return productSizeID;
    }

    public void setProductSizeID(int productSizeID) {
        this.productSizeID = productSizeID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public double getPriceAtCurrent() {
        return priceAtCurrent;
    }

    public void setPriceAtCurrent(double priceAtCurrent) {
        this.priceAtCurrent = priceAtCurrent;
    }

    // Tính thành tiền dựa trên giá JOIN được từ bảng ProductSize
    public double getTotalPrice() {
        return this.quantity * this.priceAtCurrent;
    }
}
