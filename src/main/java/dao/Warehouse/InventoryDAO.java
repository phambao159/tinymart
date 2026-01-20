package dao.Warehouse;

import model.Warehouse.Inventory;
import model.Warehouse.InventoryItemCard;

import java.sql.SQLException;
import java.util.List;

public class InventoryDAO {
    private InventoryItemCardDAO itemCardDAO;

    public InventoryDAO() {
        itemCardDAO = new InventoryItemCardDAO();
    }

    // Lấy toàn bộ inventory từ các Import đã Confirmed
    public Inventory getConfirmedInventory() throws SQLException {
        // Gọi searchItems với điều kiện rỗng để lấy tất cả
        List<InventoryItemCard> items = itemCardDAO.searchItems("", null, null);
        return new Inventory(items);
    }
}