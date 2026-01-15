package model.cashier;

import java.sql.Timestamp;

public class OrderViewModel {
    private int orderId;
    private String orderTime;
    private String cashierName;
    private String customerName;
    private double totalAmount;
    private String paymentMethod;

    public OrderViewModel(int orderId, String orderTime, String cashierName, String customerName, double totalAmount, String paymentMethod) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.cashierName = cashierName;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

    public int getOrderId() { return orderId; }
    public String getOrderTime() { return orderTime; }
    public String getCashierName() { return cashierName; }
    public String getCustomerName() { return customerName; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
}