package controller.manager;

import dao.manager.ProductSizeDAO;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.manager.ProductSize;
import model.manager.ProductSummary;

public class ProductdetailController implements Initializable {

    @FXML private ImageView imageProduct;
    @FXML private TableView<ProductSize> tableSizes; // Thêm TableView vào FXML nếu chưa có
    @FXML private TableColumn<ProductSize, String> colSize;
    @FXML private TableColumn<ProductSize, Double> colCost;
    @FXML private TableColumn<ProductSize, Integer> colSelling;
    @FXML private TableColumn<ProductSize, Integer> colStock;

    private ProductSizeDAO productSizeDAO = new ProductSizeDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cấu hình các cột TableView để khớp với thuộc tính trong model ProductSize
        colSize.setCellValueFactory(new PropertyValueFactory<>("sizeType"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        colSelling.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
    }

    /**
     * Phương thức nhận dữ liệu từ Card và đổ vào UI
     */
    public void initData(ProductSummary summary) {
        // 1. Hiển thị ảnh
        String path = "/image/" + summary.getImage();
        try {
            imageProduct.setImage(new Image(getClass().getResourceAsStream(path)));
        } catch (Exception e) {
            imageProduct.setImage(new Image(getClass().getResourceAsStream("/image/coca.png")));
        }

        // 2. Lấy dữ liệu từ Database thông qua DAO
        List<ProductSize> sizes = productSizeDAO.getByProductID(summary.getProductID());
        
        // 3. Đưa dữ liệu lên TableView
        ObservableList<ProductSize> data = FXCollections.observableArrayList(sizes);
        tableSizes.setItems(data);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        // Xử lý thêm mới size nếu cần
    }
}