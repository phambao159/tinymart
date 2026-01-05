package controller.manager.product;

import dao.manager.product.CategoryDAO;
import dao.manager.product.ProductDAO;
import dao.manager.product.PromotionDAO;
import dao.manager.product.SizeDAO;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.manager.product.Category;
import model.manager.product.ProductSummary;
import model.manager.product.Promotion;
import model.manager.product.Size;

public class ProductController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbCategory, cbSize, cbPromotion;
    @FXML
    private FlowPane productContainer;

    private final ProductDAO productDAO = new ProductDAO();
    private final String ALL_OPTION = "--- All ---";
    private Timeline searchTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Tải dữ liệu vào các ComboBox lọc
        loadFilterOptions();

        // 2. Tải danh sách sản phẩm mặc định
        applyFilters();

        // 3. Sự kiện lọc thời gian thực cho ô tìm kiếm
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (searchTimer != null) {
                searchTimer.stop();
            }
            searchTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> applyFilters()));
            searchTimer.play();
        });
    }

    private void loadFilterOptions() {
        loadCategories();
        loadPromotions();
        loadSizes();
    }

    private void applyFilters() {
        String keyword = txtSearch.getText().trim();
        String category = getFilterValue(cbCategory);
        String size = getFilterValue(cbSize);
        String promotion = getFilterValue(cbPromotion);

        renderProductList(keyword, category, size, promotion);
    }

    private String getFilterValue(ComboBox<String> cb) {
        String val = cb.getValue();
        return (val == null || val.equals(ALL_OPTION)) ? null : val;
    }

    private void renderProductList(String keyword, String category, String size, String promotion) {
        productContainer.getChildren().clear();
        List<ProductSummary> products = productDAO.getProductSummaries(keyword, category, size, promotion);

        for (ProductSummary product : products) {
            try {
                // Đảm bảo đường dẫn FXML đến file thẻ sản phẩm chính xác
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/productcard.fxml"));
                VBox productCard = loader.load();

                ProductCardController cardController = loader.getController();
                cardController.setData(product);

                cardController.setRefreshCallback(() -> applyFilters());

                productContainer.getChildren().add(productCard);
            } catch (IOException e) {
                System.err.println("Error rendering product card: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/addProduct.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Product");
            stage.initModality(Modality.APPLICATION_MODAL); // Chế độ hộp thoại (ngăn tương tác cửa sổ chính)
            stage.setScene(new Scene(root));

            // Sau khi đóng cửa sổ AddProduct, làm mới lại danh sách
            stage.setOnHiding(e -> applyFilters());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Các hàm hỗ trợ tải dữ liệu cho ComboBox ---
    private void loadCategories() {
        CategoryDAO cd = new CategoryDAO();
        ObservableList<String> list = FXCollections.observableArrayList(ALL_OPTION);
        cd.getData().forEach(c -> list.add(c.getName()));
        cbCategory.setItems(list);
        cbCategory.getSelectionModel().selectFirst();
    }

    private void loadPromotions() {
        PromotionDAO pd = new PromotionDAO();
        ObservableList<String> list = FXCollections.observableArrayList(ALL_OPTION);
        pd.getData().forEach(p -> list.add(p.getName()));
        cbPromotion.setItems(list);
        cbPromotion.getSelectionModel().selectFirst();
    }

    private void loadSizes() {
        SizeDAO sd = new SizeDAO();
        ObservableList<String> list = FXCollections.observableArrayList(ALL_OPTION);
        sd.getData().forEach(s -> list.add(s.getType()));
        cbSize.setItems(list);
        cbSize.getSelectionModel().selectFirst();
    }
}
