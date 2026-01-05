package controller.manager.product;

import dao.manager.product.CategoryDAO;
import dao.manager.product.ProductDAO;
import dao.manager.product.PromotionDAO;
import model.manager.product.Category;
import model.manager.product.Product;
import model.manager.product.Promotion;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class AddProductController implements Initializable {

    @FXML
    private TextField txtName, txtUnit;
    @FXML
    private ComboBox<Category> cbCategory;
    @FXML
    private ComboBox<Promotion> cbPromotion;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private ImageView imgPreview;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    private File selectedFile;
    private final String DEFAULT_IMAGE = "default-product.png";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
    }

    private void setupComboBoxes() {
        // Categories
        cbCategory.setItems(FXCollections.observableArrayList(categoryDAO.getData()));
        cbCategory.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category c) {
                return c == null ? "" : c.getName();
            }

            @Override
            public Category fromString(String s) {
                return null;
            }
        });

        // Promotions
        cbPromotion.setItems(FXCollections.observableArrayList(promotionDAO.getData()));
        cbPromotion.setConverter(new StringConverter<Promotion>() {
            @Override
            public String toString(Promotion p) {
                return p == null ? "No Promotion" : p.getName();
            }

            @Override
            public Promotion fromString(String s) {
                return null;
            }
        });

        // Status
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        cbStatus.getSelectionModel().selectFirst();
    }

    @FXML
    private void onChooseImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedFile = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            imgPreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        String name = txtName.getText().trim();
        Category cat = cbCategory.getValue();
        String unit = txtUnit.getText().trim();
        Promotion promo = cbPromotion.getValue();

        if (name.isEmpty() || cat == null || unit.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Please fill Name, Category and Unit!").show();
            return;
        }

        // Save Image
        String fileName = DEFAULT_IMAGE;
        if (selectedFile != null) {
            fileName = saveImageToProject(selectedFile);
        }

        // Create Product (Đảm bảo Model Product của bạn có trường PromotionID)
        Product p = new Product(0, name, cat.getCategoryID(), unit, cbStatus.getValue(), fileName);
        // Giả sử bạn thêm setPromotionID vào Model:
        // p.setPromotionID(promo != null ? promo.getPromotionID() : null);

        if (productDAO.insert(p)) {
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        }
    }

    private String saveImageToProject(File file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            String projectPath = System.getProperty("user.dir");

            // 1. Lưu vào thư mục SOURCE (để file không bị mất khi bạn tắt code)
            File srcDir = new File(projectPath + "/src/main/resources/image/manager");
            if (!srcDir.exists()) {
                srcDir.mkdirs();
            }
            File srcFile = new File(srcDir, fileName);
            Files.copy(file.toPath(), srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 2. Lưu vào thư mục TARGET/BUILD (để JavaFX thấy ảnh ngay lập tức)
            try {
                URL resourceUrl = getClass().getResource("/image/manager/");
                if (resourceUrl != null) {
                    File targetDir = new File(resourceUrl.toURI());
                    File targetFile = new File(targetDir, fileName);
                    Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✅ Đã đồng bộ ảnh sang Target (Runtime).");
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Không thể copy sang Target: " + ex.getMessage());
            }

            return fileName;
        } catch (IOException e) {
            System.err.println("❌ Lỗi lưu ảnh: " + e.getMessage());
            return DEFAULT_IMAGE;
        }
    }

    @FXML
    private void onCancel(ActionEvent e) {
        ((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
    }
}
