package controller.Warehouse;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import model.Warehouse.InventoryItemCard;

public class InventoryItemCardController {

    @FXML
    private ImageView productImage;
    @FXML
    private Label productName;
    @FXML
    private Label sizeType;
    @FXML
    private Label inboundQuantity;
    @FXML
    private Label outboundQuantity;
    @FXML
    private Label expiryDate;
    @FXML
    private Label status;
    @FXML
    private VBox cardRoot;

    public void setData(InventoryItemCard item) {
        if (item == null) {
            return;
        }

        productName.setText(item.getProductName());
        sizeType.setText(item.getSizeType());
        inboundQuantity.setText("Inbound: " + item.getInboundQuantity());
        outboundQuantity.setText("Outbound: " + item.getOutboundQuantity());
//        expiryDate.setText(item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A");
        status.setText(item.getStatus());

        // Load ảnh
        if (item.getImagePath() != null) {
            try {
                Image image = new Image(getClass().getResource(item.getImagePath()).toExternalForm());
                productImage.setImage(image);
            } catch (Exception e) {
                System.out.println("Image load failed: " + item.getImagePath());
            }
        }

        // ✅ Nếu status = inactive → làm mờ card
        if ("inactive".equalsIgnoreCase(item.getStatus())) {
            cardRoot.setOpacity(0.5);   // chỉ làm mờ toàn bộ card
            cardRoot.setDisable(true);  // disable để không cho double-click
        } else {
            cardRoot.setOpacity(1.0);   // active thì hiển thị bình thường
            cardRoot.setDisable(false);
        }
    }
}
