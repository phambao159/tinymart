package controller.manager.product;

import dao.manager.product.CategoryDAO;
import dao.manager.product.ProductDAO;
import model.manager.product.Category;
import model.manager.product.Product;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
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
    private ComboBox<String> cbStatus;
    @FXML
    private ImageView imgPreview;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private File selectedFile;
    private final String DEFAULT_IMAGE = "default-product.png";
    private Runnable refreshCallback;

    public void setOnSave(Runnable callback) {
        this.refreshCallback = callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
    }

    private void setupComboBoxes() {
        // 1. Setup Categories
        cbCategory.setItems(FXCollections.observableArrayList(categoryDAO.getData()));
        cbCategory.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category c) { 
                return (c == null) ? "" : c.getName(); 
            }
            
            @Override
            public Category fromString(String s) { // ĐÃ SỬA LỖI TYPO TẠI ĐÂY
                return null; 
            }
        });

        // 2. Status
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        cbStatus.getSelectionModel().selectFirst();
    }

    @FXML
    private void onChooseImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Product Image");
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
        String status = cbStatus.getValue();

        // Validate dữ liệu
        if (name.isEmpty() || cat == null || unit.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill Name, Category and Unit!");
            return;
        }

        // Xử lý ảnh
        String fileName = DEFAULT_IMAGE;
        if (selectedFile != null) {
            fileName = saveImageToProject(selectedFile);
        }

        // Tạo đối tượng Product (Sử dụng Constructor mới không có PromotionID)
        Product p = new Product(
            name, 
            cat.getCategoryID(), 
            unit, 
            status, 
            fileName
        );

        if (productDAO.insert(p)) {
            if (refreshCallback != null) refreshCallback.run();
            closeWindow(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add product.");
        }
    }

    private String saveImageToProject(File file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            String projectPath = System.getProperty("user.dir");

            // 1. Lưu vào thư mục resources của Source (Để lưu vĩnh viễn)
            File srcDir = new File(projectPath + "/src/main/resources/image/manager");
            if (!srcDir.exists()) srcDir.mkdirs();
            Files.copy(file.toPath(), new File(srcDir, fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 2. Đồng bộ sang thư mục Target/Build (Để hiển thị ngay lập tức mà không cần restart)
            try {
                URL resourceUrl = getClass().getResource("/image/manager/");
                if (resourceUrl != null) {
                    File targetFile = new File(new File(resourceUrl.toURI()), fileName);
                    Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception ex) {
                System.err.println("Note: Target sync skipped, image will appear after rebuild.");
            }
            return fileName;
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            return DEFAULT_IMAGE;
        }
    }

    @FXML
    private void onCancel(ActionEvent e) { 
        closeWindow(e); 
    }

    private void closeWindow(ActionEvent e) {
        ((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}