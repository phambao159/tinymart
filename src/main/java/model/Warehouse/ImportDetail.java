package model.Warehouse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ImportDetail {

    private int importDetailID;
    private int importID;
    private int productSizeID;
    private long quantity;
    private double importPrice;
    private LocalDate expiryDate;
    private String productName;
    private String sizeType;
    private String expiryDateString;

    public ImportDetail(int importDetailID, int importID, int productSizeID,
                        long quantity, double importPrice, LocalDate expiryDate) {
        this.importDetailID = importDetailID;
        this.importID = importID;
        this.productSizeID = productSizeID;
        this.quantity = quantity;
        this.importPrice = importPrice;
        this.expiryDate = expiryDate;

        // Nếu có expiryDate thì format ra chuỗi dd-MM-yyyy
        if (expiryDate != null) {
            this.expiryDateString = expiryDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
    }

    // Getters & Setters
    public int getImportDetailID() {
        return importDetailID;
    }

    public void setImportDetailID(int importDetailID) {
        this.importDetailID = importDetailID;
    }

    public int getImportID() {
        return importID;
    }

    public void setImportID(int importID) {
        this.importID = importID;
    }

    public int getProductSizeID() {
        return productSizeID;
    }

    public void setProductSizeID(int productSizeID) {
        this.productSizeID = productSizeID;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public double getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(double importPrice) {
        this.importPrice = importPrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        if (expiryDate != null) {
            this.expiryDateString = expiryDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } else {
            this.expiryDateString = null;
        }
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSizeType() {
        return sizeType;
    }

    public void setSizeType(String sizeType) {
        this.sizeType = sizeType;
    }

    public String getExpiryDateString() {
        return expiryDateString;
    }

    public void setExpiryDateString(String expiryDateString) {
        this.expiryDateString = expiryDateString;
        try {
            this.expiryDate = LocalDate.parse(expiryDateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception e) {
            this.expiryDate = null; // nếu nhập sai format thì giữ null
        }
    }
}