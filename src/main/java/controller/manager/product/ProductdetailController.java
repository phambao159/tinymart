package controller.manager.product;

import dao.manager.product.ProductSizeDAO;
import dao.manager.product.SizeDAO;
import dao.manager.product.PromotionDAO;
import java.net.URL;
import java.util.List; // FIX LỖI 1: Thêm import List
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import model.manager.product.ProductSize;
import model.manager.product.ProductSummary;
import model.manager.product.Size;
import model.manager.product.Promotion;

public class ProductdetailController implements Initializable {

    @FXML
    private ImageView imageProduct;
    @FXML
    private TableView<ProductSize> tableSizes;
    @FXML
    private TableColumn<ProductSize, String> colSize;
    @FXML
    private TableColumn<ProductSize, Double> colCost;
    @FXML
    private TableColumn<ProductSize, Double> colSelling;
    @FXML
    private TableColumn<ProductSize, Integer> colStock;

    // FIX LỖI 3: Để TableColumn là Integer (trùng với kiểu của promotionID)
    @FXML
    private TableColumn<ProductSize, Integer> colPromotion;

    @FXML
    private ComboBox<Size> cbSize;
    @FXML
    private ComboBox<Promotion> cbPromotion;
    @FXML
    private TextField txtCost, txtSelling, txtStock;
    private Button btnSave;

    private final ProductSizeDAO productSizeDAO = new ProductSizeDAO();
    private final SizeDAO sizeDAO = new SizeDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    // FIX LỖI 2: Khai báo biến allPromotions để dùng cho CellFactory
    private List<Promotion> allPromotions = promotionDAO.getProActive();

    private ProductSummary currentProduct;
    private final ObservableList<ProductSize> tableData = FXCollections.observableArrayList();
    private ProductSize editingProductSize = null;
    private Runnable refreshCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load danh sách khuyến mãi vào bộ nhớ trước
        allPromotions = promotionDAO.getData();

        setupTableView();
        setupComboBoxes();

        txtStock.setEditable(false);
        txtStock.setDisable(true);
        txtStock.setStyle("-fx-opacity: 1; -fx-background-color: #eeeeee;");

        tableSizes.setRowFactory(tv -> {
            TableRow<ProductSize> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    loadRowToForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupTableView() {
        colSize.setCellValueFactory(new PropertyValueFactory<>("sizeType"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        colSelling.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        // Trỏ vào promotionID trong Model ProductSize
        colPromotion.setCellValueFactory(new PropertyValueFactory<>("promotionID"));

        // Cập nhật CellFactory để xử lý hiển thị có điều kiện
        colPromotion.setCellFactory(column -> new TableCell<ProductSize, Integer>() {
            @Override
            protected void updateItem(Integer promoID, boolean empty) {
                super.updateItem(promoID, empty);

                // Nếu dòng trống (không có đối tượng ProductSize)
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Dòng có dữ liệu nhưng promoID là 0 hoặc null
                    if (promoID == null || promoID == 0) {
                        setText("No Promotion");
                    } else {
                        // Tìm tên từ danh sách allPromotions
                        String name = allPromotions.stream()
                                .filter(p -> p.getPromotionID() == promoID)
                                .map(Promotion::getName)
                                .findFirst()
                                .orElse("ID: " + promoID);
                        setText(name);
                    }
                }
            }
        });

        tableSizes.setItems(tableData);
    }

    private void setupComboBoxes() {
        // 1. Setup Size ComboBox
        cbSize.setItems(FXCollections.observableArrayList(sizeDAO.getData()));
        cbSize.setConverter(new StringConverter<Size>() {
            @Override
            public String toString(Size s) {
                return (s == null) ? "" : s.getType();
            }

            @Override
            public Size fromString(String string) {
                return null;
            }
        });

        // 2. Setup Promotion ComboBox với lựa chọn "No Promotion"
        ObservableList<Promotion> promoOptions = FXCollections.observableArrayList();

        // Tạo một đối tượng giả đại diện cho việc không có khuyến mãi
        Promotion noPromo = new Promotion();
        noPromo.setPromotionID(0);
        noPromo.setName("No Promotion");

        promoOptions.add(noPromo); // Thêm "No Promotion" lên đầu
        if (allPromotions != null) {
            promoOptions.addAll(allPromotions);
        }

        cbPromotion.setItems(promoOptions);
        cbPromotion.setConverter(new StringConverter<Promotion>() {
            @Override
            public String toString(Promotion p) {
                return (p == null) ? "No Promotion" : p.getName();
            }

            @Override
            public Promotion fromString(String string) {
                return null;
            }
        });

        // Mặc định chọn No Promotion
        cbPromotion.setValue(noPromo);
    }

    // Các hàm initData, onAddEntry, onClear... giữ nguyên như bài trước
    // Lưu ý: Đảm bảo trong FXML, fx:id="colPromotion" được khai báo đúng
    public void setOnSave(Runnable callback) {
        this.refreshCallback = callback;
    }

    public void initData(ProductSummary summary) {
        this.currentProduct = summary;
        if (summary != null && summary.getImage() != null) {
            loadProductImage(summary.getImage());
        }
        refreshTable();
    }

    private void loadProductImage(String imageName) {
        try {
            String path = "/image/manager/" + imageName;
            URL imageUrl = getClass().getResource(path);
            imageProduct.setImage(new Image((imageUrl != null) ? imageUrl.toExternalForm()
                    : getClass().getResource("/image/manager/default.png").toExternalForm()));
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }

    private void refreshTable() {
        if (currentProduct != null) {
            tableData.setAll(productSizeDAO.getByProductID(currentProduct.getProductID()));
        }
    }

    private void loadRowToForm(ProductSize ps) {
        if (ps == null) {
            return;
        }
        this.editingProductSize = ps;
        cbSize.getItems().stream().filter(s -> s.getSizeID() == ps.getSizeID()).findFirst().ifPresent(cbSize::setValue);
        cbPromotion.getItems().stream().filter(p -> p.getPromotionID() == ps.getPromotionID()).findFirst().ifPresent(cbPromotion::setValue);
        txtCost.setText(String.valueOf(ps.getCostPrice()));
        txtSelling.setText(String.valueOf(ps.getSellingPrice()));
        txtStock.setText(String.valueOf(ps.getStockQuantity()));
        if (btnSave != null) {
            btnSave.setText("Update");
        }
    }

    @FXML
    private void onAddEntry(ActionEvent event) {
        try {
            Size selectedSize = cbSize.getValue();
            Promotion selectedPromo = cbPromotion.getValue();
            if (selectedSize == null || txtCost.getText().isEmpty() || txtSelling.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Fill required fields.");
                return;
            }
            double cost = Double.parseDouble(txtCost.getText());
            double selling = Double.parseDouble(txtSelling.getText());
            int promoID = (selectedPromo != null) ? selectedPromo.getPromotionID() : 0;

            if (editingProductSize == null) {
                ProductSize ps = new ProductSize();
                ps.setProductID(currentProduct.getProductID());
                ps.setSizeID(selectedSize.getSizeID());
                ps.setCostPrice(cost);
                ps.setSellingPrice(selling);
                ps.setPromotionID(promoID);
                if (productSizeDAO.insert(ps)) {
                    showSuccess("Added successfully.");
                }
            } else {
                editingProductSize.setSizeID(selectedSize.getSizeID());
                editingProductSize.setCostPrice(cost);
                editingProductSize.setSellingPrice(selling);
                editingProductSize.setPromotionID(promoID);
                if (productSizeDAO.update(editingProductSize)) {
                    showSuccess("Updated successfully.");
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid data.");
        }
    }

    private void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", message);
        refreshTable();
        if (refreshCallback != null) {
            refreshCallback.run();
        }
        onClear(null);
    }

    private void onClear(ActionEvent event) {
        editingProductSize = null;
        cbSize.getSelectionModel().clearSelection();
        cbPromotion.getSelectionModel().clearSelection();
        txtCost.clear();
        txtSelling.clear();
        txtStock.clear();
        if (btnSave != null) {
            btnSave.setText("Add");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.show();
    }

    @FXML
    private void onDelete(ActionEvent event) {
    }
}
