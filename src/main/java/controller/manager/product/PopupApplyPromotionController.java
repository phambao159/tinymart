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
import model.manager.product.ProductSummary;

public class PopupApplyPromotionController {
    @FXML private Label lbProductName;
    @FXML private ComboBox<ProductSize> cbSize;
    private final ProductSizeDAO psDAO = new ProductSizeDAO();
    private Consumer<ProductSize> callback;

    public void setData(ProductSummary product, Consumer<ProductSize> callback) {
        this.callback = callback;
        lbProductName.setText(product.getName());
        
        List<ProductSize> sizes = psDAO.getByProductID(product.getProductID());
        cbSize.setItems(FXCollections.observableArrayList(sizes));
        cbSize.setConverter(new StringConverter<ProductSize>() {
            @Override public String toString(ProductSize o) { return o == null ? "" : o.getSizeType(); }
            @Override public ProductSize fromString(String s) { return null; }
        });
    }

    @FXML
    private void onAdd(ActionEvent event) {
        ProductSize selected = cbSize.getValue();
        if (selected != null && callback != null) {
            callback.accept(selected);
            ((Stage) cbSize.getScene().getWindow()).close();
        }
    }

    @FXML private void onCancel(ActionEvent event) {
        ((Stage) cbSize.getScene().getWindow()).close();
    }
}