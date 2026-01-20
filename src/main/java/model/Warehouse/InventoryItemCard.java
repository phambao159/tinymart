package model.Warehouse;

import java.time.LocalDate;

public class InventoryItemCard {
    private int productSizeId;
    private String productName;
    private String sizeType;
    private String status;
    private LocalDate expiryDate;
    private int inboundQuantity;     // Tổng Quantity
    private int outboundQuantity;    // Tổng ShelfQuantity
    private String imageFileName;    // chỉ lưu tên file, ví dụ "coca.png"

    public InventoryItemCard(int productSizeId, String productName, String sizeType,
                             String status, LocalDate expiryDate,
                             int inboundQuantity, int outboundQuantity, String imageFileName) {
        this.productSizeId = productSizeId;
        this.productName = productName;
        this.sizeType = sizeType;
        this.status = status;
        this.expiryDate = expiryDate;
        this.inboundQuantity = inboundQuantity;
        this.outboundQuantity = outboundQuantity;
        this.imageFileName = imageFileName;
    }

    
    public int getProductSizeId() { return productSizeId; }
    public String getProductName() { return productName; }
    public String getSizeType() { return sizeType; }
    public String getStatus() { return status; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getInboundQuantity() { return inboundQuantity; }
    public int getOutboundQuantity() { return outboundQuantity; }

    
    public String getImagePath() {
        if (imageFileName == null || imageFileName.isEmpty()) {
            return null;
        }
        return "/image.manager/" + imageFileName;
    }
}