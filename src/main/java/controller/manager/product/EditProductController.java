package controller.manager.product;

import dao.manager.product.CategoryDAO;
import dao.manager.product.ProductDAO;
import model.manager.product.Category;
import model.manager.product.Product;
import model.manager.product.ProductSummary;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.util.List;
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

public class EditProductController implements Initializable {

    @FXML
    private TextField txtID, txtName, txtUnit;
    @FXML
    private ComboBox<Category> cbCategory;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private ImageView imgPreview;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private File selectedFile;
    private String currentImageName;
    private Runnable refreshCallback;

    public void setOnSave(Runnable callback) {
        this.refreshCallback = callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization handled in initData hoặc setupComboBoxes
    }

    private void setupComboBoxes() {
        // 1. Setup Category Converter
        cbCategory.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category c) {
                return (c == null) ? "" : c.getName();
            }

            @Override
            public Category fromString(String s) {
                return null;
            }
        });

        // 2. Load Data from DB
        loadCategoryData();

        // 3. Load Status
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
    }

    private void loadCategoryData() {
        try {
            List<Category> list = categoryDAO.getData();
            if (list != null) {
                cbCategory.setItems(FXCollections.observableArrayList(list));
            }
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
    }

    /**
     * Nhận dữ liệu từ màn hình danh sách truyền sang
     */
    public void initData(ProductSummary summary) {
        setupComboBoxes(); // Khởi tạo các ComboBox trước khi set value

        txtID.setText(String.valueOf(summary.getProductID()));
        txtName.setText(summary.getName());
        txtUnit.setText(summary.getUnit());
        cbStatus.setValue(summary.getStatus());
        currentImageName = summary.getImage();

        // Set default Category dựa trên ID
        if (cbCategory.getItems() != null) {
            for (Category cat : cbCategory.getItems()) {
                if (cat.getCategoryID() == summary.getCategoryID()) {
                    cbCategory.setValue(cat);
                    break;
                }
            }
        }

        loadProductImage(currentImageName);
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        String name = txtName.getText().trim();
        String unit = txtUnit.getText().trim();
        Category selectedCat = cbCategory.getValue();
        String status = cbStatus.getValue();

        if (name.isEmpty() || unit.isEmpty() || selectedCat == null || status == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all information!");
            return;
        }

        // Xử lý tên file ảnh: Nếu có chọn mới thì lưu file mới, nếu không giữ lại tên cũ
        String fileName = (selectedFile != null) ? saveImageToProject(selectedFile) : currentImageName;

        // Tạo object Product (Sử dụng constructor 6 tham số: ID, Name, CatID, Unit, Status, Image)
        Product p = new Product(
                Integer.parseInt(txtID.getText()),
                name,
                selectedCat.getCategoryID(),
                unit,
                status,
                fileName
        );

        if (productDAO.update(p)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
            if (refreshCallback != null) {
                refreshCallback.run();
            }
            closeWindow(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Update Failed", "Check database connection.");
        }
    }

    private void loadProductImage(String imageName) {
        try {
            // Đảm bảo đường dẫn chính xác tới thư mục tài nguyên
            String imagePath = "/image/manager/" + imageName;
            URL imgUrl = getClass().getResource(imagePath);
            
            if (imgUrl != null) {
                imgPreview.setImage(new Image(imgUrl.toExternalForm()));
            } else {
                // Fallback nếu không tìm thấy ảnh
                URL defaultUrl = getClass().getResource("/image/manager/default-product.png");
                if (defaultUrl != null) imgPreview.setImage(new Image(defaultUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    private String saveImageToProject(File file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            File srcDir = new File(System.getProperty("user.dir") + "/src/main/resources/image/manager");
            if (!srcDir.exists()) {
                srcDir.mkdirs();
            }
            Files.copy(file.toPath(), new File(srcDir, fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            System.err.println("Save image error: " + e.getMessage());
            return currentImageName;
        }
    }

    @FXML
    private void onChooseImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Product Image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            imgPreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to disable this product?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Disable");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                int id = Integer.parseInt(txtID.getText());
                // Logic soft delete: Cập nhật status thành Inactive
                if (productDAO.delete(id)) { 
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                    closeWindow(event);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to disable the product.");
                }
            }
        });
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