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
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.manager.product.ProductSummary;

public class ProductController implements Initializable {
    // Định nghĩa hằng số cho mục mặc định
    private static final String CAT_DEFAULT = "Category";
    private static final String SIZE_DEFAULT = "Size";
    private static final String PROM_DEFAULT = "Promotion";

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbCategory, cbSize, cbPromotion;
    @FXML private FlowPane productContainer;
    @FXML private ScrollPane viewProduct;
    @FXML private RadioButton rbActive, rbInactive;
    @FXML private ToggleGroup statusGroup;
    @FXML private HBox hSearch, hAdd;

    private final ProductDAO productDAO = new ProductDAO();
    private Timeline searchTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadFilterOptions();
        
        // 1. Listener cho ô tìm kiếm (Debounce 300ms)
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (searchTimer != null) searchTimer.stop();
            searchTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> applyFilters()));
            searchTimer.play();
        });

        // 2. Listener cho các ComboBox
        cbCategory.setOnAction(e -> applyFilters());
        cbSize.setOnAction(e -> applyFilters());
        cbPromotion.setOnAction(e -> applyFilters());

        // 3. Listener cho ToggleGroup (RadioButton)
        statusGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        applyFilters(); // Tải dữ liệu lần đầu
    }

    /**
     * Thu thập tất cả các giá trị từ bộ lọc và yêu cầu render lại danh sách
     */
    private void applyFilters() {
        String keyword = txtSearch.getText().trim();
        
        // Lấy giá trị ComboBox (null nếu là mặc định)
        String category = getFilterValue(cbCategory, CAT_DEFAULT);
        String size = getFilterValue(cbSize, SIZE_DEFAULT);
        String promotion = getFilterValue(cbPromotion, PROM_DEFAULT);

        // Lấy trạng thái từ RadioButton
        // Nếu database của bạn dùng String "Active"/"Inactive", hãy truyền chuỗi
        String status = rbActive.isSelected() ? "Active" : "Inactive";

        renderProductList(keyword, category, size, promotion, status);
    }

    private String getFilterValue(ComboBox<String> cb, String defaultValue) {
        String val = cb.getValue();
        return (val == null || val.equals(defaultValue)) ? null : val;
    }

    private void renderProductList(String keyword, String category, String size, String promotion, String status) {
        productContainer.getChildren().clear();
        
        // Cập nhật hàm gọi DAO để nhận thêm tham số status
        List<ProductSummary> products = productDAO.getProductSummaries(keyword, category, size, promotion, status);

        for (ProductSummary product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/productcard.fxml"));
                VBox productCard = loader.load();

                ProductCardController cardController = loader.getController();
                cardController.setData(product);
                
                // Callback để load lại khi có thay đổi (Sửa/Xóa từ Card)
                cardController.setRefreshCallback(this::applyFilters);

                productContainer.getChildren().add(productCard);
            } catch (IOException e) {
                System.err.println("Error rendering product card: " + e.getMessage());
            }
        }
    }

    // --- Tải dữ liệu cho bộ lọc ComboBox ---
    private void loadFilterOptions() {
        // Load Category
        ObservableList<String> catList = FXCollections.observableArrayList(CAT_DEFAULT);
        new CategoryDAO().getData().forEach(c -> catList.add(c.getName()));
        cbCategory.setItems(catList);
        cbCategory.getSelectionModel().selectFirst();

        // Load Size
        ObservableList<String> sizeList = FXCollections.observableArrayList(SIZE_DEFAULT);
        new SizeDAO().getData().forEach(s -> sizeList.add(s.getType()));
        cbSize.setItems(sizeList);
        cbSize.getSelectionModel().selectFirst();

        // Load Promotion
        ObservableList<String> promList = FXCollections.observableArrayList(PROM_DEFAULT);
        new PromotionDAO().getProActive().forEach(p -> promList.add(p.getName()));
        cbPromotion.setItems(promList);
        cbPromotion.getSelectionModel().selectFirst();
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
            stage.setOnHiding(e -> applyFilters()); // Refresh sau khi thêm mới
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}