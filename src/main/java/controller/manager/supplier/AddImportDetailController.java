/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller.manager.supplier;

import dao.manager.product.ProductSizeDAO;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.manager.product.ProductSize;
import model.manager.product.ProductSummary;
import model.manager.supplier.ImportDetail;

/**
 * FXML Controller class
 *
 * @author user
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

    /**
     * Initializes the controller class.
     */
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
        String selectedName = cbSize.getValue();
        String qtyStr = txtQuantity.getText().trim();

        if (selectedName != null && !qtyStr.isEmpty()) {
            try {
                int qty = Integer.parseInt(qtyStr);

                // Find the original object to get ID and Price
                ProductSize selectedObj = fullSizeList.stream()
                        .filter(s -> s.getSizeType().equals(selectedName))
                        .findFirst()
                        .orElse(null);

                if (selectedObj != null && onSaveCallback != null) {
                    ImportDetail detail = new ImportDetail();
                    detail.setProductID(this.ProductID);
                    detail.setProductSizeID(selectedObj.getProductSizeID());
                    detail.setProductName(this.lbProductName.getText()); // Lấy từ Label trên popup
                    detail.setSizeName(selectedObj.getSizeType());
                    detail.setQuantity(qty);
                    detail.setImportPrice(selectedObj.getCostPrice());

                    onSaveCallback.accept(detail);
                    onCancel(event);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid quantity format");
            }
        }
    }

    public void setData(int ProductID, String productName, Consumer<ImportDetail> callback) {
        this.ProductID = ProductID;
        this.lbProductName.setText(productName);
        this.onSaveCallback = callback;
        loadSize();
    }

    public void loadSize() {
        ProductSizeDAO pDAO = new ProductSizeDAO();
        this.fullSizeList = pDAO.getSizesByProductId(this.ProductID);

        if (fullSizeList != null && !fullSizeList.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (ProductSize ps : fullSizeList) {
                names.add(ps.getSizeType()); // Get S, M, L...
            }
            cbSize.setItems(FXCollections.observableArrayList(names));
            cbSize.getSelectionModel().selectFirst();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.show();
    }

}
