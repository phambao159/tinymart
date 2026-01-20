package controller.manager.product;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.manager.product.ProductSummary;
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
    private Label lbSelling;

    @FXML
    private Label lbStock;

    @FXML
    private Label lbStatus;

    @FXML
    private VBox cardProduct;

    private ProductSummary summary;

    // callback used to refresh parent screen
    private Runnable refreshCallback;

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    public void setData(ProductSummary summary) {
        this.summary = summary;

        final String PLACEHOLDER_PATH = "/image/manager/coca.png";
        String productImagePath = "/image/manager/" + summary.getImage();

        InputStream stream = getClass().getResourceAsStream(productImagePath);

        // fallback to placeholder
        if (stream == null) {
            System.err.println("⚠️ WARNING: Image not found at: " + productImagePath + ". Using default image: " + PLACEHOLDER_PATH);
            stream = getClass().getResourceAsStream(PLACEHOLDER_PATH);
        }

        // if still null → critical error
        if (stream == null) {
            System.err.println("❌ CRITICAL ERROR: Neither product image nor placeholder image could be found (" + PLACEHOLDER_PATH + ").");
            return;
        }

        Image image = new Image(stream);

        if (imgProduct == null) {
            System.err.println("❌ FXML INJECTION ERROR: imgProduct is NULL.");
            return;
        }

        imgProduct.setImage(image);

        // set labels
        lbName.setText(summary.getName());


        lbSelling.setText("Selling: " + String.format("%.2f", summary.getMinSellingPrice()) + "$");

        lbStock.setText("Stock: " + summary.getTotalStockQuantity());

        lbStatus.setText(summary.getStatus());

        // styling based on status
        if ("Active".equals(summary.getStatus())) {
            lbStatus.getStyleClass().add("status-available");
        } else {
            lbStatus.getStyleClass().add("status-unavailable");
        }
    }

    @FXML
    private void onDetail(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/ProductDetail.fxml"));
            Parent root = loader.load();

            ProductdetailController detailController = loader.getController();
            detailController.initData(this.summary);
            detailController.setOnSave(() -> {
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Product Detail: " + summary.getName());
            stage.setScene(new Scene(root));

            // open as modal popup
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error opening product detail popup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEdit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/EditProduct.fxml"));
            Parent root = loader.load();

            EditProductController editController = loader.getController();
            editController.initData(this.summary);
            editController.setOnSave(() -> {
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Edit Product: " + summary.getName());
            stage.setScene(new Scene(root));

            // open as modal popup
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error opening edit form: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
