package model.manager.order;

import java.time.LocalDateTime;

/**
 * Model class for Order entity based on SQL structure.
 */
public class Order {

    private int orderID;              // OrderID (int UNSIGNED, PK)
    private LocalDateTime orderDateTime; // OrderDateTime (datetime)
    private int employeeID;           // EmployeeID (int)
    private Integer customerID;       // CustomerID (int, có thể NULL)
    private double totalAmount;       // TotalAmount (decimal(8,2))
    private double discountAmount;    // DiscountAmount (Khuyến mãi/Voucher)
    private double pointDiscount;     // TRƯỜNG MỚI: Giảm giá bằng điểm tích lũy
    private String paymentMethod;     // PaymentMethod (varchar(255))
    private String customerName;      
    private String employeeName;      

    // Constructor không tham số
    public Order() {
    }

    // Constructor đầy đủ tham số (Cập nhật để bao gồm pointDiscount)
    public Order(int orderID, LocalDateTime orderDateTime, int employeeID, Integer customerID,
            double totalAmount, double discountAmount, double pointDiscount, String paymentMethod) {
        this.orderID = orderID;
        this.orderDateTime = orderDateTime;
        this.employeeID = employeeID;
        this.customerID = customerID;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.pointDiscount = pointDiscount;
        this.paymentMethod = paymentMethod;
    }

    // Getter và Setter cho PointDiscount
    public double getPointDiscount() {
        return pointDiscount;
    }

    public void setPointDiscount(double pointDiscount) {
        this.pointDiscount = pointDiscount;
    }

    // Getter và Setter cho các trường cũ
    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(LocalDateTime orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    /**
     * Phương thức tính số tiền khách thực trả sau khi trừ tất cả các loại giảm giá.
     */
    public double getFinalAmount() {
        return totalAmount - discountAmount - pointDiscount;
    }

    @Override
    public String toString() {
        return "Order{" + "orderID=" + orderID 
                + ", orderDateTime=" + orderDateTime
                + ", totalAmount=" + totalAmount 
                + ", pointDiscount=" + pointDiscount
                + ", paymentMethod=" + paymentMethod + '}';
    }
}