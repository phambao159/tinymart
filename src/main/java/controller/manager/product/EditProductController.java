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
    private TextField txtID; // Đảm bảo @FXML để tránh NullPointerException
    @FXML
    private TextField txtName; // txtUnit đã xóa khỏi FXML
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
        // Initialization handled in initData
    }

    private void setupComboBoxes() {
        cbCategory.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category c) {
                return (c == null) ? "" : c.getName();
            }
            @Override
            public Category fromString(String s) { return null; }
        });
        loadCategoryData();
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
    }

    private void loadCategoryData() {
        List<Category> list = categoryDAO.getData();
        if (list != null) {
            cbCategory.setItems(FXCollections.observableArrayList(list));
        }
    }

    public void initData(ProductSummary summary) {
        setupComboBoxes();

        txtID.setText(String.valueOf(summary.getProductID()));
        txtName.setText(summary.getName());
        cbStatus.setValue(summary.getStatus());
        currentImageName = summary.getImage();

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
        resetStyles();

        int id = Integer.parseInt(txtID.getText());
        String name = txtName.getText().trim();
        Category selectedCat = cbCategory.getValue();
        String status = cbStatus.getValue();
        String unit = null; // Gán null theo yêu cầu

        // 1. Kiểm tra Not Null
        if (name.isEmpty()) {
            showError(txtName, "Product name cannot be empty!");
            return;
        }

        // 2. Kiểm tra trùng tên (Loại trừ sản phẩm hiện tại)
        if (productDAO.isNameExistsForEdit(name, id)) {
            showError(txtName, "This product name is already taken by another product!");
            return;
        }

        if (selectedCat == null) {
            showError(cbCategory, "Please select a category!");
            return;
        }

        if (status == null) {
            showError(cbStatus, "Please select a status!");
            return;
        }

        // 3. Xử lý ảnh
        String fileName = (selectedFile != null) ? saveImageToProject(selectedFile) : currentImageName;

        Product p = new Product(id, name, selectedCat.getCategoryID(), unit, status, fileName);

        if (productDAO.update(p)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
            if (refreshCallback != null) refreshCallback.run();
            closeWindow(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Update Failed", "Database error occurred.");
        }
    }

    // --- Helpers ---

    private void showError(Control control, String message) {
        control.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5px;");
        control.requestFocus();
        showAlert(Alert.AlertType.ERROR, "Validation Error", message);
    }

    private void resetStyles() {
        txtName.setStyle("");
        cbCategory.setStyle("");
        cbStatus.setStyle("");
    }

    private void loadProductImage(String imageName) {
        try {
            String imagePath = "/image/manager/" + (imageName == null ? "default-product.png" : imageName);
            URL imgUrl = getClass().getResource(imagePath);
            if (imgUrl != null) {
                imgPreview.setImage(new Image(imgUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    private String saveImageToProject(File file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            File srcDir = new File(System.getProperty("user.dir") + "/src/main/resources/image/manager");
            if (!srcDir.exists()) srcDir.mkdirs();
            Files.copy(file.toPath(), new File(srcDir, fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            return currentImageName;
        }
    }

    @FXML
    private void onChooseImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            imgPreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Disable this product?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (productDAO.delete(Integer.parseInt(txtID.getText()))) {
                    if (refreshCallback != null) refreshCallback.run();
                    closeWindow(event);
                }
            }
        });
    }

    @FXML
    private void onCancel(ActionEvent e) { closeWindow(e); }

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