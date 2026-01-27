package controller.Warehouse;

import dao.Warehouse.InventoryItemDetailDAO;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Warehouse.InventoryItemCard;
import model.Warehouse.InventoryItemDetailRow;

public class InventoryItemCardController {

    @FXML
    private ImageView productImage;
    @FXML
    private Label productName;
    @FXML
    private Label sizeType;
    @FXML
    private Label stockLabel;   // ✅ hiển thị tổng stock
    @FXML
    private Label shelfLabel;   // ✅ thêm label hiển thị ShelfQuantity
    @FXML
    private VBox cardRoot;

    public void setData(InventoryItemCard item) {
        if (item == null) {
            return;
        }

        productName.setText(item.getProductName());
        sizeType.setText(item.getSizeType());
        stockLabel.setText("Stock: " + item.getStock());
        shelfLabel.setText("On Shelf: " + item.getShelfQuantity()); // ✅ hiển thị riêng ShelfQuantity

        // ✅ Logic đổi viền nếu ShelfQuantity < 10
        if (item.getShelfQuantity() < 10) {
            cardRoot.setStyle("-fx-border-color: red; -fx-border-width: 2; -fx-border-radius: 5;");
        } else {
            cardRoot.setStyle(""); // reset style nếu không cần viền đỏ
        }

        // Load ảnh
        if (item.getImagePath() != null) {
            try {
                Image image = new Image(getClass().getResource(item.getImagePath()).toExternalForm());
                productImage.setImage(image);
            } catch (Exception e) {
                System.out.println("Image load failed: " + item.getImagePath());
            }
        }

        // ✅ Double-click mở chi tiết
        cardRoot.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openDetailWindow(item);
            }
        });
    }

    private void openDetailWindow(InventoryItemCard item) {
        try {
            // Lấy dữ liệu chi tiết từ DAO
            InventoryItemDetailDAO detailDAO = new InventoryItemDetailDAO();
            List<InventoryItemDetailRow> details = detailDAO.getDetailsByProductSizeId(item.getProductSizeId());

            // Load FXML chi tiết
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Warehouse/InventoryItemDetail.fxml"));
            Parent root = loader.load();

            // Set dữ liệu cho controller chi tiết
            InventoryItemDetailController controller = loader.getController();
            controller.setData(item.getProductName(), item.getSizeType(), "active",
                   item.getProductSizeId(), details);

            // Mở cửa sổ mới
            Stage stage = new Stage();
            stage.setTitle("Product Details");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}