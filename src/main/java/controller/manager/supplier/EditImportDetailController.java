package controller.manager.supplier;

import dao.manager.product.ProductSizeDAO;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.manager.product.ProductSize;
import model.manager.supplier.ImportDetail;

public class EditImportDetailController implements Initializable {

    @FXML private Label lbProductName;
    @FXML private Label lbPrice; // Đã đổi thành Label
    @FXML private ComboBox<String> cbSize;
    @FXML private TextField txtQuantity;

    private ImportDetail currentDetail;
    private Consumer<ImportDetail> onUpdateCallback;
    private Consumer<ImportDetail> onDeleteCallback;
    private List<ProductSize> fullSizeList;
    @FXML
    private DatePicker dpExpiryDate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Lắng nghe sự kiện thay đổi Size trên ComboBox để cập nhật Label Price
        cbSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updatePriceLabel(newVal);
            }
        });
    }

    public void setData(ImportDetail detail, int productID, Consumer<ImportDetail> onUpdate, Consumer<ImportDetail> onDelete) {
        this.currentDetail = detail;
        this.onUpdateCallback = onUpdate;
        this.onDeleteCallback = onDelete;

        this.lbProductName.setText(detail.getProductName());
        this.txtQuantity.setText(String.valueOf(detail.getQuantity()));
        this.dpExpiryDate.setValue(detail.getExpiryDate());
        // Load danh sách size trước khi set giá trị cho Label/ComboBox
        loadSize(productID, detail.getSizeName());
        
        // Hiển thị giá ban đầu
        this.lbPrice.setText(String.format("%,.2f", detail.getImportPrice()));
    }

    private void loadSize(int productID, String currentSizeName) {
        ProductSizeDAO pDAO = new ProductSizeDAO();
        this.fullSizeList = pDAO.getSizesByProductId(productID);

        if (fullSizeList != null && !fullSizeList.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (ProductSize ps : fullSizeList) {
                names.add(ps.getSizeType());
            }
            cbSize.setItems(FXCollections.observableArrayList(names));
            cbSize.setValue(currentSizeName);
        }
    }

    // Hàm cập nhật Label giá khi chọn Size khác
    private void updatePriceLabel(String sizeName) {
        if (fullSizeList != null) {
            fullSizeList.stream()
                .filter(s -> s.getSizeType().equals(sizeName))
                .findFirst()
                .ifPresent(s -> lbPrice.setText(String.format("%,.2f", s.getCostPrice())));
        }
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        String selectedSize = cbSize.getValue();
        String qtyStr = txtQuantity.getText().trim();

        if (selectedSize == null || qtyStr.isEmpty()) {
            showAlert("Please enter quantity.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();

            // Lấy đối tượng Size được chọn để lấy ID và Giá mới
            ProductSize selectedObj = fullSizeList.stream()
                    .filter(s -> s.getSizeType().equals(selectedSize))
                    .findFirst()
                    .orElse(null);

            if (selectedObj != null && onUpdateCallback != null) {
                currentDetail.setProductSizeID(selectedObj.getProductSizeID());
                currentDetail.setSizeName(selectedObj.getSizeType());
                currentDetail.setQuantity(qty);
                // Cập nhật lại giá nhập theo Size mới (lấy từ costPrice của ProductSize)
                currentDetail.setImportPrice(selectedObj.getCostPrice());
                currentDetail.setExpiryDate(dpExpiryDate.getValue());

                onUpdateCallback.accept(currentDetail);
                onCancel(event);
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid quantity. Please enter a positive integer.");
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remove this item from list?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (onDeleteCallback != null) onDeleteCallback.accept(currentDetail);
                onCancel(event);
            }
        });
    }

    @FXML
    private void onCancel(ActionEvent event) {
        ((Stage) lbProductName.getScene().getWindow()).close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.show();
    }
}