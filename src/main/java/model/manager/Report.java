package model.manager;

public class Report {
    private String label; 
    private double value; 

    // Constructor mặc định
    public Report() {
    }

    // Constructor dùng cho cả doanh thu và số lượng
    public Report(String label, double value) {
        this.label = label;
        this.value = value;
    }

    // Getters và Setters
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}