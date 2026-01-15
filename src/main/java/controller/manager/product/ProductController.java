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
import javafx.scene.control.ScrollPane;
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
    // Định nghĩa hằng số cho mục mặc định
    private static final String CAT_DEFAULT = "Category";
    private static final String SIZE_DEFAULT = "Size";
    private static final String PROM_DEFAULT = "Promotion";

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbCategory, cbSize, cbPromotion;
    @FXML private FlowPane productContainer;
    @FXML private ScrollPane viewProduct;

    private final ProductDAO productDAO = new ProductDAO();
    private Timeline searchTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadFilterOptions();
        
        // Listener cho ô tìm kiếm với Delay 300ms (Debounce)
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (searchTimer != null) searchTimer.stop();
            searchTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> applyFilters()));
            searchTimer.play();
        });

        // Listener cho các ComboBox
        cbCategory.setOnAction(e -> applyFilters());
        cbSize.setOnAction(e -> applyFilters());
        cbPromotion.setOnAction(e -> applyFilters());
        
        applyFilters(); // Load dữ liệu lần đầu
    }

    private void applyFilters() {
        String keyword = txtSearch.getText().trim();
        
        // Logic lấy giá trị lọc: Nếu là giá trị mặc định thì truyền null vào DAO
        String category = cbCategory.getValue();
        if (CAT_DEFAULT.equals(category)) category = null;

        String size = cbSize.getValue();
        if (SIZE_DEFAULT.equals(size)) size = null;

        String promotion = cbPromotion.getValue();
        if (PROM_DEFAULT.equals(promotion)) promotion = null;

        renderProductList(keyword, category, size, promotion);
    }

    private void renderProductList(String keyword, String category, String size, String promotion) {
        productContainer.getChildren().clear();
        List<ProductSummary> products = productDAO.getProductSummaries(keyword, category, size, promotion);

        for (ProductSummary product : products) {
            try {
                // Đảm bảo đường dẫn FXML chính xác (thêm / ở đầu nếu cần)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/productcard.fxml"));
                VBox productCard = loader.load();

                ProductCardController cardController = loader.getController();
                cardController.setData(product);
                cardController.setRefreshCallback(this::applyFilters); // Dùng method reference

                productContainer.getChildren().add(productCard);
            } catch (IOException e) {
                System.err.println("Error rendering product card: " + e.getMessage());
            }
        }
    }

    // --- Các hàm tải dữ liệu ---
    private void loadCategories() {
        CategoryDAO cd = new CategoryDAO();
        ObservableList<String> list = FXCollections.observableArrayList(CAT_DEFAULT);
        cd.getData().forEach(c -> list.add(c.getName()));
        cbCategory.setItems(list);
        cbCategory.getSelectionModel().selectFirst();
    }

    private void loadPromotions() {
        PromotionDAO pd = new PromotionDAO();
        ObservableList<String> list = FXCollections.observableArrayList(PROM_DEFAULT);
        pd.getProActive().forEach(p -> list.add(p.getName()));
        cbPromotion.setItems(list);
        cbPromotion.getSelectionModel().selectFirst();
    }

    private void loadSizes() {
        SizeDAO sd = new SizeDAO();
        ObservableList<String> list = FXCollections.observableArrayList(SIZE_DEFAULT);
        sd.getData().forEach(s -> list.add(s.getType()));
        cbSize.setItems(list);
        cbSize.getSelectionModel().selectFirst();
    }

    private void loadFilterOptions() {
        loadCategories();
        loadPromotions();
        loadSizes();
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/addProduct.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> applyFilters());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
