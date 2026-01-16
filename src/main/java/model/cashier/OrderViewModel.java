package model.cashier;

public class OrderViewModel {
    private int orderId;
    private String orderTime;
    private String cashierName;
    private String customerName;
    private String customerPhone;
    private double totalAmount;
    private String paymentMethod;

    public OrderViewModel(int orderId, String orderTime, String cashierName, String customerName, String customerPhone, double totalAmount, String paymentMethod) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.cashierName = cashierName;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

    public int getOrderId() { return orderId; }
    public String getOrderTime() { return orderTime; }
    public String getCashierName() { return cashierName; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; } 
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
}