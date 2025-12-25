package controller.manager;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.manager.ProductSummary; 
import java.io.InputStream; 
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProductCardController {

    @FXML
    private ImageView imgProduct;

    @FXML
    private Label lbName;

    @FXML
    private Label lbCost;

    @FXML
    private Label lbSelling;

    @FXML
    private Label lbStock;

    @FXML
    private Label lbStatus;
    @FXML
    private VBox cardProduct;
    private ProductSummary summary;

    public void setData(ProductSummary summary) {
        this.summary = summary;

        final String PLACEHOLDER_PATH = "/image/coca.png";
        

        String productImagePath = "/image/" + summary.getImage();

        InputStream stream = getClass().getResourceAsStream(productImagePath);

        if (stream == null) {
            System.err.println("⚠️ CẢNH BÁO: Không tìm thấy ảnh tại: " + productImagePath + ". Dùng ảnh mặc định: " + PLACEHOLDER_PATH);
            stream = getClass().getResourceAsStream(PLACEHOLDER_PATH);
        }

 
        if (stream == null) {
            System.err.println("❌ LỖI NGHIÊM TRỌNG: Không tìm thấy cả ảnh sản phẩm và ảnh mặc định (" + PLACEHOLDER_PATH + ").");
            return; 
        }
        

        Image image = new Image(stream);


        if (imgProduct == null) {
            System.err.println("❌ LỖI FXML INJECTION: imgProduct đang là NULL.");
            return; 
        }
        
        imgProduct.setImage(image);
        lbName.setText(summary.getName());
        
        lbCost.setText("Cost : N/A"); 
        

        lbSelling.setText("Selling : " + String.format("%.2f", summary.getMinSellingPrice()) + "$");
        

        lbStock.setText("Stock : " + summary.getTotalStockQuantity());
        
        lbStatus.setText(summary.getStatus());


        if ("Active".equals(summary.getStatus())) {
            lbStatus.getStyleClass().add("status-available");
        } else {
            lbStatus.getStyleClass().add("status-unavailable");
        }
    }

    @FXML
    private void onDetail(ActionEvent event) {
        try {
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/ProductDetail.fxml"));
            Parent root = loader.load();

            
            ProductdetailController detailController = loader.getController();
            detailController.initData(this.summary); 

            
            Stage stage = new Stage();
            stage.setTitle("Product Detail: " + summary.getName());
            stage.setScene(new Scene(root));
            
            // Thiết lập chế độ Modal (ngăn tương tác với cửa sổ chính khi đang mở popup)
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Lỗi khi mở popup chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
    }
}