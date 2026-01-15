package controller.manager.product;

import dao.manager.product.ProductSizeDAO;
import java.util.List;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.manager.product.ProductSize;

public class PopupEditPromotionController {

    @FXML
    private Label lbProductName;
    @FXML
    private ComboBox<ProductSize> cbSize;
    private final ProductSizeDAO psDAO = new ProductSizeDAO();

    private Consumer<ProductSize> saveCallback;
    private Runnable deleteCallback;

    public void setData(ProductSize productSize, Consumer<ProductSize> saveCallback, Runnable deleteCallback) {
        this.saveCallback = saveCallback;
        this.deleteCallback = deleteCallback;

        lbProductName.setText(productSize.getProductName());

        // Load danh sách tất cả các size của sản phẩm đó
        List<ProductSize> sizes = psDAO.getByProductID(productSize.getProductID());
        if (sizes == null || sizes.isEmpty()) {
            System.out.println("Warning: No sizes found in database for this product!");
        }
        cbSize.setItems(FXCollections.observableArrayList(sizes));

        cbSize.setConverter(new StringConverter<ProductSize>() {
            @Override
            public String toString(ProductSize o) {
                return o == null ? "" : o.getSizeType();
            }

            @Override
            public ProductSize fromString(String s) {
                return null;
            }
        });

        for (ProductSize s : cbSize.getItems()) {
            if (s.getProductSizeID() == productSize.getProductSizeID()) {
                cbSize.getSelectionModel().select(s);
                break;
            }
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        ProductSize selected = cbSize.getValue();
        if (selected != null && saveCallback != null) {
            saveCallback.accept(selected);
            closeStage();
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        if (deleteCallback != null) {
            deleteCallback.run();
            closeStage();
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        ((Stage) cbSize.getScene().getWindow()).close();
    }
}
