package model.Warehouse;

import java.util.List;

public class Inventory {
    private List<InventoryItemCard> items;

    public Inventory(List<InventoryItemCard> items) {
        this.items = items;
    }

    public List<InventoryItemCard> getItems() {
        return items;
    }
}