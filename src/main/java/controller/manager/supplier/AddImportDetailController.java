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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.manager.product.ProductSize;
import model.manager.supplier.ImportDetail;

/**
 * FXML Controller class cho việc thêm chi tiết phiếu nhập từ popup
 */
public class AddImportDetailController implements Initializable {

    @FXML
    private Label lbProductName;
    @FXML
    private ComboBox<String> cbSize;
    @FXML
    private TextField txtQuantity;
    
    private int ProductID;
    private Consumer<ImportDetail> onSaveCallback;
    private List<ProductSize> fullSizeList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void onCancel(ActionEvent event) {
        Stage stage = (Stage) lbProductName.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onAdd(ActionEvent event) {
        String selectedSizeName = cbSize.getValue();
        String qtyStr = txtQuantity.getText().trim();

        if (selectedSizeName == null) {
            showAlert("Please select a size.");
            return;
        }

        if (qtyStr.isEmpty()) {
            showAlert("Please enter quantity.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                showAlert("Quantity must be greater than 0.");
                return;
            }

            // Tìm đối tượng ProductSize tương ứng để lấy ID và Giá gốc (Cost Price)
            ProductSize selectedObj = fullSizeList.stream()
                    .filter(s -> s.getSizeType().equals(selectedSizeName))
                    .findFirst()
                    .orElse(null);

            if (selectedObj != null && onSaveCallback != null) {
                ImportDetail detail = new ImportDetail();
                detail.setProductID(this.ProductID);
                detail.setProductSizeID(selectedObj.getProductSizeID());
                detail.setProductName(this.lbProductName.getText());
                detail.setSizeName(selectedObj.getSizeType());
                detail.setQuantity(qty);
                detail.setImportPrice(selectedObj.getCostPrice());
                
                // --- CẬP NHẬT TẠI ĐÂY: Tự động set ShelfQuantity là 0 ---
                detail.setShelfQuantity(0);

                onSaveCallback.accept(detail);
                onCancel(event);
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid quantity format. Please enter a number.");
        }
    }

    public void setData(int ProductID, String productName, Consumer<ImportDetail> callback) {
        this.ProductID = ProductID;
        this.lbProductName.setText(productName);
        this.onSaveCallback = callback;
        loadSize();
    }

    private void loadSize() {
        ProductSizeDAO pDAO = new ProductSizeDAO();
        this.fullSizeList = pDAO.getSizesByProductId(this.ProductID);

        if (fullSizeList != null && !fullSizeList.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (ProductSize ps : fullSizeList) {
                names.add(ps.getSizeType());
            }
            cbSize.setItems(FXCollections.observableArrayList(names));
            cbSize.getSelectionModel().selectFirst();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Warning");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}