package controller.Warehouse;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import model.Warehouse.InventoryReport;

public class InventoryReportCardController {

    @FXML private ImageView productImage;
    @FXML private Label productName;
    @FXML private Label sizeType;
    @FXML private Label expireDate;
    @FXML private Label quantity;
    @FXML private Label status;
    @FXML private Label actionType;

    /**
     * Nhận dữ liệu từ InventoryReport và hiển thị lên card
     */
    public void setData(InventoryReport report) {
        productName.setText(report.getProductName());
        sizeType.setText("Size: " + report.getSizeType());
        expireDate.setText("Expire: " + report.getExpireDate());
        quantity.setText("Qty: " + report.getQuantity());
        status.setText(report.getStatus());
        actionType.setText(report.getActionType());

        if (report.getProductImage() != null) {
            productImage.setImage(report.getProductImage());
        }
    }
}