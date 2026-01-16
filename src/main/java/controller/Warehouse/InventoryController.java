package controller.Warehouse;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import dao.Warehouse.InventoryItemCardDAO;
import model.Warehouse.InventoryItemCard;

public class InventoryController {

    @FXML
    private FlowPane productGrid;
    @FXML
    private TextField searchNameField;
    @FXML
    private TextField minQuantityField;
    @FXML
    private TextField maxQuantityField;

    private List<InventoryItemCard> allItems;

    @FXML
    public void initialize() {
        InventoryItemCardDAO dao = new InventoryItemCardDAO();
        System.out.println("✅ DAO đã được khởi tạo trong Controller!");
        allItems = dao.getAllCardItems();
        System.out.println("✅ Rendering " + allItems.size() + " items...");
        renderGrid(allItems); // Hiển thị sản phẩm ngay khi mở trang
    }

    @FXML
    private void onSearch() {
        String name = searchNameField.getText().toLowerCase();

        int minQty = 0;
        int maxQty = Integer.MAX_VALUE;

        try {
            if (!minQuantityField.getText().isEmpty()) {
                minQty = Integer.parseInt(minQuantityField.getText());
            }
            if (!maxQuantityField.getText().isEmpty()) {
                maxQty = Integer.parseInt(maxQuantityField.getText());
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Lỗi nhập số lượng: " + e.getMessage());
        }

        List<InventoryItemCard> filtered = filterItems(name, minQty, maxQty);
        renderGrid(filtered);
    }

    private List<InventoryItemCard> filterItems(String name, int minQty, int maxQty) {
        return allItems.stream()
                .filter(item -> item.getProductName().toLowerCase().contains(name)
                        && item.getShelfQuantity() >= minQty
                        && item.getShelfQuantity() <= maxQty)
                .collect(Collectors.toList());
    }

    private void renderGrid(List<InventoryItemCard> items) {
        productGrid.getChildren().clear();
        for (InventoryItemCard item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Warehouse/InventoryItemCard.fxml"));
                VBox card = loader.load();
                InventoryItemCardController controller = loader.getController();
                controller.setData(item);
                productGrid.getChildren().add(card);
                System.out.println("✅ Card added to grid: " + item.getProductName());
            } catch (IOException e) {
                System.out.println("❌ Card load error: " + e.getMessage());
            }
        }

        System.out.println("📊 Rendering " + items.size() + " items...");
        items.forEach(item -> System.out.println(item.getProductName() + " - " + item.getShelfQuantity()));
    }
}