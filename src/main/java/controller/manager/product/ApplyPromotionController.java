package controller.manager.product;

import dao.manager.product.ProductDAO;
import dao.manager.product.PromotionDAO;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.manager.product.ProductSize;
import model.manager.product.ProductSummary;
import model.manager.product.Promotion;

public class ApplyPromotionController implements Initializable {

    @FXML
    private ComboBox<Promotion> cbPromotion;
    @FXML
    private TextField txtSearch;
    @FXML
    private FlowPane productContainer;
    private Label lblSelectedCount;
    @FXML
    private TableView<ProductSize> tbSelectedProducts;
    @FXML
    private TableColumn<ProductSize, Integer> colID;
    @FXML
    private TableColumn<ProductSize, String> colProductName;
    @FXML
    private TableColumn<ProductSize, String> colSize;

    private final ObservableList<ProductSize> selectedList = FXCollections.observableArrayList();
    private final ProductDAO productDAO = new ProductDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        setupTable();
        loadPromotions();
        loadProductCards("");

        // Tìm kiếm card sản phẩm thời gian thực
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadProductCards(newVal));

        // LẮNG NGHE THAY ĐỔI TRÊN COMBOBOX
        cbPromotion.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAppliedProducts(newVal.getPromotionID());
            }
        });

        // MẶC ĐỊNH CHỌN KHUYẾN MÃI ĐẦU TIÊN
        if (!cbPromotion.getItems().isEmpty()) {
            cbPromotion.getSelectionModel().selectFirst();
        }
        tbSelectedProducts.setRowFactory(tv -> {
            TableRow<ProductSize> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ProductSize selectedProduct = row.getItem();
                    openEditPopup(selectedProduct);
                }
            });
            return row;
        });
    }

    private void loadAppliedProducts(int promotionID) {
        // Gọi DAO để lấy danh sách ProductSize theo PromotionID
        // Lưu ý: Bạn cần viết hàm 'getProductSizesByPromotion' trong ProductDAO hoặc PromotionDAO
        List<ProductSize> appliedList = promotionDAO.getProductSizesByPromotion(promotionID);

        selectedList.setAll(appliedList);
    }

    private void setupTable() {
        colID.setCellValueFactory(new PropertyValueFactory<>("productSizeID"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("sizeType"));
        tbSelectedProducts.setItems(selectedList);
    }

    private void loadPromotions() {
        cbPromotion.setItems(FXCollections.observableArrayList(promotionDAO.getProActive()));
        cbPromotion.setConverter(new StringConverter<Promotion>() {
            @Override
            public String toString(Promotion p) {
                return (p == null) ? "" : p.getName();
            }

            @Override
            public Promotion fromString(String s) {
                return null;
            }
        });
    }

    private void loadProductCards(String keyword) {
        productContainer.getChildren().clear();
        List<ProductSummary> products = productDAO.getProductSummaries(keyword, null, null, null,"Active");
        for (ProductSummary p : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/productcard_promotion.fxml"));
                VBox card = loader.load();
                Productcard_promotionController ctrl = loader.getController();
                // Truyền hàm addProductToSelection vào Card
                ctrl.setData(p, this::addProductToSelection);
                productContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Hàm nhận ProductSize từ Card gửi về
    private void addProductToSelection(ProductSize size) {
        boolean exists = selectedList.stream().anyMatch(s -> s.getProductSizeID() == size.getProductSizeID());
        if (exists) {
            new Alert(Alert.AlertType.WARNING, "This size is already selected!").show();
        } else {
            selectedList.add(size);
            if (lblSelectedCount != null) {
                lblSelectedCount.setText("Selected: " + selectedList.size());
            }
        }
    }

    @FXML
    private void onApply(ActionEvent event) {
        Promotion promo = cbPromotion.getValue();

        List<Integer> ids = selectedList.stream().map(ProductSize::getProductSizeID).collect(Collectors.toList());
        if (promotionDAO.applyPromotionToProducts(promo.getPromotionID(), ids)) {
            new Alert(Alert.AlertType.INFORMATION, "Success!").showAndWait();
            onCancel(event);
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        ((Stage) cbPromotion.getScene().getWindow()).close();
    }

    @FXML
    private void onDeleteAll(ActionEvent event) {
        // 1. Kiểm tra nếu danh sách đã trống sẵn thì không làm gì cả
        if (selectedList.isEmpty()) {
            return;
        }

        // 2. Hiện hộp thoại xác nhận xóa
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Clear all products");
        alert.setContentText("Are you sure you want to remove all selected products from this table?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // 3. Xóa sạch danh sách ObservableList
            selectedList.clear();

            // 4. Cập nhật lại nhãn đếm (nếu có)
            if (lblSelectedCount != null) {
                lblSelectedCount.setText("Selected: 0");
            }
        }
    }

    private void openEditPopup(ProductSize selectedProduct) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/popupEditPromotion.fxml"));
            Parent root = loader.load();

            PopupEditPromotionController controller = loader.getController();

            controller.setData(selectedProduct,
                    (updatedSize) -> {

                        int index = selectedList.indexOf(selectedProduct);
                        if (index != -1) {
                            selectedList.set(index, updatedSize);
                        }
                    },
                    () -> {
                        // Logic Xóa: Gỡ khỏi danh sách
                        selectedList.remove(selectedProduct);
                    }
            );

            Stage stage = new Stage();
            stage.setTitle("Edit Selection");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbSelectedProducts.getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
