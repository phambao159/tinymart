package controller.Warehouse;

import dao.Warehouse.InventoryDAO;
import dao.Warehouse.InventoryItemCardDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.Warehouse.Inventory;
import model.Warehouse.InventoryItemCard;

import java.util.List;

public class InventoryController {

    @FXML
    private FlowPane productGrid;
    @FXML
    private TextField searchNameField;
    @FXML
    private TextField minQuantityField;
    @FXML
    private TextField maxQuantityField;

    private InventoryDAO inventoryDAO;
    private InventoryItemCardDAO itemCardDAO;

    public InventoryController() {
        this.inventoryDAO = new InventoryDAO();
        this.itemCardDAO = new InventoryItemCardDAO();
    }

    @FXML
    public void initialize() {
        loadCompletedInventory();
    }

    private void loadCompletedInventory() {
        try {
            Inventory inventory = inventoryDAO.getCompletedInventory();
            displayItems(inventory.getItems());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayItems(List<InventoryItemCard> items) {
        productGrid.getChildren().clear();
        System.out.println("Controller: Found items = " + items.size());

        for (InventoryItemCard item : items) {
            System.out.println("Controller: Loading card for " + item.getProductName());
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Warehouse/InventoryItemCard.fxml"));
                VBox card = loader.load();

                if (card == null) {
                    System.out.println("Controller: card is null for " + item.getProductName());
                    continue;
                }

                InventoryItemCardController controller = loader.getController();
                if (controller == null) {
                    System.out.println("Controller: controller is null for " + item.getProductName());
                    continue;
                }

                controller.setData(item);
                productGrid.getChildren().add(card);
                System.out.println("Controller: card added for " + item.getProductName());

            } catch (Exception e) {
                System.out.println("Controller: Error loading card for " + item.getProductName());
                e.printStackTrace();
            }
        }

        System.out.println("Controller: Grid children count = " + productGrid.getChildren().size());
    }

    @FXML
    private void onSearch() {
        System.out.println("Controller: onSearch triggered");
        String name = (searchNameField != null) ? searchNameField.getText().trim() : "";
        String minStr = (minQuantityField != null) ? minQuantityField.getText().trim() : "";
        String maxStr = (maxQuantityField != null) ? maxQuantityField.getText().trim() : "";

        Integer stockMin = null, stockMax = null;
        try {
            if (!minStr.isEmpty()) {
                stockMin = Integer.parseInt(minStr);
            }
            if (!maxStr.isEmpty()) {
                stockMax = Integer.parseInt(maxStr);
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Controller: Invalid stock range input");
        }

        try {
            List<InventoryItemCard> items = itemCardDAO.searchItems(name, stockMin, stockMax);
            System.out.println("Controller: searchItems returned " + items.size() + " items");
            displayItems(items);
        } catch (Exception e) {
            System.out.println("Controller: Error in onSearch");
            e.printStackTrace();
        }
    }
}
