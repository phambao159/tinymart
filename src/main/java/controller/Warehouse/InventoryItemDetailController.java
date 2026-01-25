package controller.Warehouse;

import dao.Warehouse.InventoryItemDetailDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import model.Warehouse.InventoryItemDetailRow;

import java.util.List;

public class InventoryItemDetailController {

    @FXML
    private Label productNameLabel;
    @FXML
    private Label sizeTypeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private FlowPane expiryFlowPane;
    @FXML
    private TextField shelfInput;

    private InventoryItemDetailDAO inventoryItemDetailDAO = new InventoryItemDetailDAO();
    private String currentProductName;
    private String currentSizeType;
    private String currentStatus;
    private int currentProductSizeId;

    // ✅ Khớp với InventoryItemCardController
    public void setData(String productName, String sizeType, String status,
                        int productSizeId, List<InventoryItemDetailRow> details) {
        this.currentProductSizeId = productSizeId;
        this.currentProductName = productName;
        this.currentSizeType = sizeType;
        this.currentStatus = status;

        productNameLabel.setText(productName);
        sizeTypeLabel.setText(sizeType);
        statusLabel.setText(status);

        expiryFlowPane.getChildren().clear();

        for (InventoryItemDetailRow row : details) {
            VBox card = new VBox(5);
            card.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 8; "
                        + "-fx-background-color: #f9f9f9; -fx-background-radius: 8; "
                        + "-fx-padding: 10; -fx-effect: dropshadow(gaussian, #aaa, 3, 0, 2, 2);");
            card.setPrefWidth(200);

            Label info = new Label(productName + ", " + sizeType);
            info.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
            Label date = new Label("Date: " + (row.getExpiryDate() != null ? row.getExpiryDate() : "N/A"));
            Label qty = new Label("Qty: " + row.getQuantity());
            Label shelf = new Label("Shelf: " + row.getShelfQuantity());

            card.getChildren().addAll(info, date, qty, shelf);
            expiryFlowPane.getChildren().add(card);
        }
    }

    @FXML
    private void onAddToShelf() {
        try {
            int amount = Integer.parseInt(shelfInput.getText().trim());
            System.out.println("[DEBUG] onAddToShelf: amount entered = " + amount);

            if (amount <= 0) {
                System.out.println("  ❌ Invalid amount (<=0)");
                new Alert(Alert.AlertType.WARNING, "Please enter a positive number!").showAndWait();
                return;
            }

            boolean success = inventoryItemDetailDAO.updateShelfQuantity(currentProductSizeId, amount);

            if (success) {
                System.out.println("  ✅ updateShelfQuantity returned true");
                new Alert(Alert.AlertType.INFORMATION, "Moved " + amount + " items to shelf!").showAndWait();
                shelfInput.clear();

                List<InventoryItemDetailRow> refreshedDetails =
                        inventoryItemDetailDAO.getDetailsByProductSizeId(currentProductSizeId);

                // ✅ Gọi lại setData với đủ 5 tham số
                setData(currentProductName, currentSizeType, currentStatus,
                        currentProductSizeId, refreshedDetails);

            } else {
                System.out.println("  ❌ updateShelfQuantity returned false");
                new Alert(Alert.AlertType.WARNING, "Invalid amount! Not enough Quantity.").showAndWait();
            }
        } catch (NumberFormatException e) {
            System.out.println("  ❌ NumberFormatException: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Please enter a valid number!").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error while moving to shelf!").showAndWait();
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) productNameLabel.getScene().getWindow();
        stage.close();
    }
}