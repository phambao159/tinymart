package controller.Warehouse;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Warehouse.InventoryItemDetailRow;

import java.util.List;

public class InventoryItemDetailController {

    @FXML private Label productNameLabel;
    @FXML private Label sizeTypeLabel;
    @FXML private Label statusLabel;
    @FXML private FlowPane expiryFlowPane;

    public void setData(String productName, String sizeType, String status, List<InventoryItemDetailRow> details) {
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
            Label date = new Label("Date: " + row.getExpiryDate());
            Label qty = new Label("Qty: " + row.getQuantity());
            Label shelf = new Label("Shelf: " + row.getShelfQuantity());

            card.getChildren().addAll(info, date, qty, shelf);
            expiryFlowPane.getChildren().add(card);
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) productNameLabel.getScene().getWindow();
        stage.close();
    }
}