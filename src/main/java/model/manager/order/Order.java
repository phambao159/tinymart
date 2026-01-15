package model.manager.order;

import java.time.LocalDateTime;

/**
 * Model class for Order entity based on SQL structure.
 */
public class Order {

    private int orderID;             // OrderID (int UNSIGNED, PK)
    private LocalDateTime orderDateTime; // OrderDateTime (datetime)
    private int employeeID;          // EmployeeID (int)
    private Integer customerID;      // CustomerID (int, có thể NULL)
    private double totalAmount;      // TotalAmount (decimal(8,2))
    private double discountAmount;   // DiscountAmount (decimal(8,2))
    private String paymentMethod;    // PaymentMethod (varchar(255))
    private String customerName; // Thêm mới
    private String employeeName; // Thêm mới

    // Getter và Setter cho 2 trường mới
    // Constructor không tham số
    public Order() {
    }

    // Constructor đầy đủ tham số
    public Order(int orderID, LocalDateTime orderDateTime, int employeeID, Integer customerID,
            double totalAmount, double discountAmount, String paymentMethod) {
        this.orderID = orderID;
        this.orderDateTime = orderDateTime;
        this.employeeID = employeeID;
        this.customerID = customerID;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.paymentMethod = paymentMethod;
    }

    // Getter và Setter
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

    @Override
    public String toString() {
        return "Order{" + "orderID=" + orderID + ", orderDateTime=" + orderDateTime
                + ", totalAmount=" + totalAmount + ", paymentMethod=" + paymentMethod + '}';
    }
}
