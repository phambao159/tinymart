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
        setupComboBoxes();
    }

    private void setupComboBoxes() {
        // 1. Cài đặt cách hiển thị chữ cho Category
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

        // 2. Load dữ liệu
        try {
            List<Category> list = categoryDAO.getData();
            if (list != null) {
                cbCategory.setItems(FXCollections.observableArrayList(list));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load danh mục: " + e.getMessage());
        }

        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
    }

    public void initData(ProductSummary summary) {
        txtID.setText(String.valueOf(summary.getProductID()));
        txtName.setText(summary.getName());
        txtUnit.setText(summary.getUnit());
        cbStatus.setValue(summary.getStatus());
        currentImageName = summary.getImage();

        for (Category c : cbCategory.getItems()) {
            if (c.getCategoryID() == summary.getCategoryID()) {
                cbCategory.setValue(c);
                break;
            }
        }

        // --- SỬA PHẦN LOAD IMAGE TẠI ĐÂY ---
        try {
            String imagePath = "/image/manager/" + currentImageName;
            java.io.InputStream stream = getClass().getResourceAsStream(imagePath);

            if (stream != null) {
                imgPreview.setImage(new Image(stream));
            } else {
                // Nếu không tìm thấy, load ảnh mặc định
                System.err.println("Không thấy ảnh: " + imagePath);
                java.io.InputStream defaultStream = getClass().getResourceAsStream("/image/manager/coca.jpg");
                if (defaultStream != null) {
                    imgPreview.setImage(new Image(defaultStream));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi load ảnh: " + e.getMessage());
        }
    }

    @FXML
    private void onChooseImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        selectedFile = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            imgPreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        String fileName = currentImageName;
        if (selectedFile != null) {
            fileName = saveImageToProject(selectedFile);
        }

        Product p = new Product(
                Integer.parseInt(txtID.getText()),
                txtName.getText(),
                cbCategory.getValue().getCategoryID(),
                txtUnit.getText(),
                cbStatus.getValue(),
                fileName
        );

        if (productDAO.update(p)) {
            showAlert("Success", "Product updated successfully!");
            if (refreshCallback != null) {
                refreshCallback.run();
            }
            closeWindow(event);
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
            return currentImageName;
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this product?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (productDAO.delete(Integer.parseInt(txtID.getText()))) {
                    // Gọi lệnh làm mới trước khi đóng cửa sổ
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                    closeWindow(event);
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

    private void showAlert(String title, String content) {
        new Alert(Alert.AlertType.INFORMATION, content).showAndWait();
    }
}
