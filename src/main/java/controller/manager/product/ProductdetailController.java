package controller.manager.product;

import dao.manager.product.ProductSizeDAO;
import dao.manager.product.SizeDAO;
import java.net.URL;
import java.util.List;
import java.util.Optional;
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

    @FXML
    private ComboBox<Size> cbSize;
    @FXML
    private TextField txtCost, txtSelling, txtStock;
    private Button btnSave; // Bạn nên đặt fx:id cho nút Add/Save để đổi tên nút nếu muốn

    private ProductSizeDAO productSizeDAO = new ProductSizeDAO();
    private SizeDAO sizeDAO = new SizeDAO();
    private ProductSummary currentProduct;
    private ObservableList<ProductSize> tableData = FXCollections.observableArrayList();

    // BIẾN QUAN TRỌNG: Lưu đối tượng đang được chọn để sửa
    private ProductSize editingProductSize = null;
    private Runnable refreshCallback;

    public void setOnSave(Runnable callback) {
        this.refreshCallback = callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Cấu hình bảng
        colSize.setCellValueFactory(new PropertyValueFactory<>("sizeType"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        colSelling.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        tableSizes.setItems(tableData);

        // 2. Nạp dữ liệu ComboBox
        loadSizeList();

        // 3. THIẾT LẬP DOUBLE CLICK CHO BẢNG
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

    // Hàm đưa dữ liệu từ dòng được chọn xuống Form để sửa
    private void loadRowToForm(ProductSize ps) {
        this.editingProductSize = ps;

        // Chọn đúng Size trong ComboBox
        for (Size s : cbSize.getItems()) {
            if (s.getSizeID() == ps.getSizeID()) {
                cbSize.setValue(s);
                break;
            }
        }

        txtCost.setText(String.valueOf(ps.getCostPrice()));
        txtSelling.setText(String.valueOf(ps.getSellingPrice()));
        txtStock.setText(String.valueOf(ps.getStockQuantity()));

        if (btnSave != null) {
            btnSave.setText("Update");
        }
    }

    private void loadSizeList() {
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
    }

    public void initData(ProductSummary summary) {
        this.currentProduct = summary;
        String path = "/image/manager/" + summary.getImage();
        try {
            imageProduct.setImage(new Image(getClass().getResourceAsStream(path)));
        } catch (Exception e) {
            imageProduct.setImage(new Image(getClass().getResourceAsStream("/image/manager/coca.png")));
        }
        refreshTable();
    }

    private void refreshTable() {
        if (currentProduct != null) {
            tableData.setAll(productSizeDAO.getByProductID(currentProduct.getProductID()));
        }
    }

    @FXML
    private void onAddEntry(ActionEvent event) {
        try {
            Size selectedSize = cbSize.getValue();
            if (selectedSize == null || txtCost.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please fill all fields!");
                return;
            }

            double cost = Double.parseDouble(txtCost.getText());
            double selling = Double.parseDouble(txtSelling.getText());
            int stock = Integer.parseInt(txtStock.getText());

            if (editingProductSize == null) {

                ProductSize ps = new ProductSize();
                ps.setProductID(currentProduct.getProductID());
                ps.setSizeID(selectedSize.getSizeID());
                ps.setCostPrice(cost);
                ps.setSellingPrice(selling);
                ps.setStockQuantity(stock);

                if (productSizeDAO.insert(ps)) {
                    refreshTable();
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                    onClear(null);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Add failed! Size may already exist.");
                }
            } else {

                editingProductSize.setSizeID(selectedSize.getSizeID());
                editingProductSize.setCostPrice(cost);
                editingProductSize.setSellingPrice(selling);
                editingProductSize.setStockQuantity(stock);

                if (productSizeDAO.update(editingProductSize)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Updated successfully!");
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                    refreshTable();
                    onClear(null);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Update failed!");
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid number format!");
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        ProductSize selected = tableSizes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a row to delete!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Are you sure you want to delete size: " + selected.getSizeType() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (productSizeDAO.delete(selected.getProductSizeID())) {
                refreshTable();
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                onClear(null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Delete failed!");
            }
        }
    }

    private void onClear(ActionEvent event) {
        editingProductSize = null; // Quan trọng: Reset trạng thái sửa về thêm mới
        cbSize.getSelectionModel().clearSelection();
        txtCost.clear();
        txtSelling.clear();
        txtStock.clear();
        if (btnSave != null) {
            btnSave.setText("Add");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
