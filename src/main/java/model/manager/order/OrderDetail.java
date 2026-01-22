package model.manager.order;

public class OrderDetail {

    private int orderDetailID;
    private int orderID;
    private int productSizeID;
    private int quantity;

    // 3 trường mới thay thế hoàn toàn cho unitPrice
    private double originalPrice; // Giá niêm yết (dùng để hiển thị giảm giá)
    private double sellingPrice;  // Giá bán thực tế (thay thế cho unitPrice cũ)
    private double unitCost;      // Giá vốn (để tính lợi nhuận)

    // Các trường mở rộng để hiển thị (Lấy qua JOIN)
    private String productName;
    private String typeName;

    public OrderDetail() {
    }

    public OrderDetail(int orderDetailID, int orderID, int productSizeID, int quantity,
            double originalPrice, double sellingPrice, double unitCost) {
        this.orderDetailID = orderDetailID;
        this.orderID = orderID;
        this.productSizeID = productSizeID;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.sellingPrice = sellingPrice;
        this.unitCost = unitCost;
    }

    // --- Logic Business ---
    /**
     * Tính thành tiền dựa trên giá bán thực tế
     */
    public double getSubTotal() {
        return sellingPrice * quantity;
    }

    /**
     * Tính lợi nhuận gộp cho mục hàng này (Tùy chọn cho báo cáo)
     */
    public double getGrossProfit() {
        return (sellingPrice - unitCost) * quantity;
    }

    // --- Getters and Setters ---
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

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
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
}
