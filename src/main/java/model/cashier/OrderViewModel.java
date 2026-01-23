package model.cashier;

public class OrderViewModel {

    private int orderId;
    private String orderTime;
    private String cashierName;
    private String customerName;
    private String customerPhone;
    private int customerPoints;
    private double totalAmount;
    private double totalDiscount;
    private String paymentMethod;
    private double pointDiscount;

    public OrderViewModel(int orderId, String orderTime, String cashierName, String customerName, String customerPhone, int customerPoints, double totalAmount, double totalDiscount, double pointDiscount, String paymentMethod) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.cashierName = cashierName;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerPoints = customerPoints;
        this.totalAmount = totalAmount;
        this.totalDiscount = totalDiscount;
        this.pointDiscount = pointDiscount;
        this.paymentMethod = paymentMethod;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public String getCashierName() {
        return cashierName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public int getCustomerPoints() {
        return customerPoints;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public double getPointDiscount() {
        return pointDiscount;
    }
}
