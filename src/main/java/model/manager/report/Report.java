package model.manager.report;

public class Report {

    // Thuộc tính cũ (Dùng cho biểu đồ và Top 10)
    private String label;
    private double value;

    // Thuộc tính mới (Dùng cho Dashboard tổng lực)
    private double rev30;
    private double revPrev30;
    private int ordersToday;
    private int ordersYesterday;
    private int custMonth;
    private int custPrevMonth;
    private String topCategory;

    // CONSTRUCTOR 1: Dùng cho biểu đồ (Revenue Chart, Top Selling)
    public Report(String label, double value) {
        this.label = label;
        this.value = value;
    }

    // CONSTRUCTOR 2: Dùng cho Dashboard tổng quát
    public Report(double rev30, double revPrev30, int ordersToday,
            int ordersYesterday, int custMonth, int custPrevMonth, String topCategory) {
        this.rev30 = rev30;
        this.revPrev30 = revPrev30;
        this.ordersToday = ordersToday;
        this.ordersYesterday = ordersYesterday;
        this.custMonth = custMonth;
        this.custPrevMonth = custPrevMonth;
        this.topCategory = topCategory;
    }

    // --- Getters cho thuộc tính cũ ---
    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }

    // --- Getters cho Dashboard ---
    public double getRev30() {
        return rev30;
    }

    public double getRevPrev30() {
        return revPrev30;
    }

    public int getOrdersToday() {
        return ordersToday;
    }

    public int getOrdersYesterday() {
        return ordersYesterday;
    }

    public int getCustMonth() {
        return custMonth;
    }

    public int getCustPrevMonth() {
        return custPrevMonth;
    }

    public String getTopCategory() {
        return topCategory;
    }
}
