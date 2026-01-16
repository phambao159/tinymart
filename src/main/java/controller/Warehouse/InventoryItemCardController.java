/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller.Warehouse;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import model.Warehouse.InventoryItemCard;

public class InventoryItemCardController {

    @FXML
    private ImageView productImage;
    @FXML
    private Label productName;
    @FXML
    private Label sizeType;
    @FXML
    private Label shelfQuantity;
    @FXML
    private Label expiryDate;
    @FXML
    private Label status;

    public void setData(InventoryItemCard item) {
        productName.setText(item.getProductName());
        sizeType.setText(item.getSizeType());
        shelfQuantity.setText("Stock: " + item.getShelfQuantity());
        expiryDate.setText("Expiry: " + item.getExpiryDate());
        status.setText(item.getStatus());

        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                // ✅ Load ảnh từ thư mục resources /image/manager/
                String imagePath = "/image/manager/" + item.getImagePath();
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                productImage.setImage(image);
            } catch (Exception e) {
                System.out.println("Image load error: " + e.getMessage());
            }
        }
    }
}
