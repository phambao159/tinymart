package controller.manager.supplier;

import java.io.IOException;
import java.io.InputStream;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import model.manager.product.ProductSummary;

import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.manager.supplier.ImportDetail;

public class ProductCardController {

    @FXML
    private VBox cardProduct;
    @FXML
    private ImageView imgProduct;
    @FXML
    private Label lbName;


    private ProductSummary product;
    private Consumer<ImportDetail> onProductAdded;

    public void setData(ProductSummary product, Consumer<ImportDetail> onProductAdded) {
        this.product = product;
        this.onProductAdded = onProductAdded;
        lbName.setText(product.getName());


        // Xử lý đường dẫn ảnh linh hoạt hơn
        String imageName = product.getImage();
        if (imageName == null || imageName.isEmpty()) {
            imageName = "coca.png"; // Ảnh mặc định nếu DB trống
        }

        // Đảm bảo đường dẫn bắt đầu bằng /image/
        String fullPath = "/image/manager/" + imageName.replace("/image/manager/", "");

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/addImportDetail.fxml"));
            Parent root = loader.load();
            AddImportDetailController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.setTitle("Create New Import Voucher");
            stage.setScene(new Scene(root));
            controller.setData(product.getProductID(), product.getName(), (ImportDetail detail) -> {
                // When the popup finishes, call the main screen's callback
                if (onProductAdded != null) {
                    onProductAdded.accept(detail);
                }
            });
            
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(cardProduct.getScene().getWindow());
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load Add Import form").show();
        }
    }
}
