package controller.manager.product;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.manager.product.ProductSize;
import model.manager.product.ProductSummary;

public class Productcard_promotionController {
    @FXML private VBox cardProduct;
    @FXML private Label lbName;

    private ProductSummary product;
    private Consumer<ProductSize> onAddClicked;
    @FXML
    private ImageView imgProduct;

     public void setData(ProductSummary product, Consumer<ProductSize> callback) {
        this.product = product;
        this.onAddClicked = callback;
        lbName.setText(product.getName());

        // Xử lý đường dẫn ảnh linh hoạt hơn
        String imageName = product.getImage();
        if (imageName == null || imageName.isEmpty()) {
            imageName = "coca.png"; // Ảnh mặc định nếu DB trống
        }

        // Đảm bảo đường dẫn bắt đầu bằng /image/
        String fullPath = "/image/manager/" + imageName;

        try {
            InputStream stream = getClass().getResourceAsStream(fullPath);
            if (stream != null) {
                imgProduct.setImage(new Image(stream));
            } else {
                // Nếu vẫn không thấy, thử load ảnh mặc định tuyệt đối
                imgProduct.setImage(new Image(getClass().getResourceAsStream("/image/manager/coca.png")));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load ảnh: " + e.getMessage());
        }
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/popupApplyPromotion.fxml"));
            Parent root = loader.load();
            PopupApplyPromotionController popupCtrl = loader.getController();

            // Nhận ProductSize từ Popup, gắn thêm tên SP rồi gửi về màn hình chính
            popupCtrl.setData(product, (selectedSize) -> {
                selectedSize.setProductName(product.getName()); // Gắn tên để hiển thị trên bảng
                if (onAddClicked != null) onAddClicked.accept(selectedSize);
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}