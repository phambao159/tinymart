package dao.Warehouse;

import util.DBConnection;
import model.Warehouse.InventoryItemCard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemCardDAO {

    public List<InventoryItemCard> getAllCardItems() {
        List<InventoryItemCard> list = new ArrayList<>();
        DBConnection db = new DBConnection();

        try (Connection con = db.getConnect()) {
            String sql = "SELECT p.Name AS ProductName, s.Type AS SizeType, " +
                         "inv.ExpiryDate, inv.ShelfQuantity, p.Status, p.Image " +
                         "FROM Inventory inv " +
                         "JOIN ProductSize ps ON inv.ProductSizeID = ps.ProductSizeID " +
                         "JOIN Product p ON ps.ProductID = p.ProductID " +
                         "JOIN Size s ON ps.SizeID = s.SizeID";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                InventoryItemCard card = new InventoryItemCard(
                    rs.getString("ProductName"),
                    rs.getString("SizeType"),
                    rs.getDate("ExpiryDate").toString(),
                    rs.getInt("ShelfQuantity"),
                    rs.getString("Status"),
                    rs.getString("Image")
                );
                System.out.println("Loaded: " + card.getProductName() + " - " + card.getShelfQuantity());
                list.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}