package dao.Warehouse;

import util.DBConnection;
import model.Warehouse.Inventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public List<Inventory> getAllInventory() {
        List<Inventory> list = new ArrayList<>();
        DBConnection db = new DBConnection();

        String sql = "SELECT inv.InventoryID, inv.ProductSizeID, p.Name AS ProductName, "
                + "s.Type AS SizeType, inv.ExpiryDate, inv.ShelfQuantity, p.Status, p.Image "
                + "FROM Inventory inv "
                + "JOIN ProductSize ps ON inv.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID";

        try (Connection con = db.getConnect(); PreparedStatement stmt = con.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Date expiry = rs.getDate("ExpiryDate");
                String expiryStr = (expiry != null) ? expiry.toString() : "";

                Inventory item = new Inventory(
                        rs.getInt("InventoryID"),
                        rs.getInt("ProductSizeID"),
                        rs.getString("ProductName"),
                        rs.getString("SizeType"),
                        expiryStr,
                        rs.getInt("ShelfQuantity"), // ✅ dùng long thay vì int
                        rs.getString("Status"),
                        rs.getString("Image")
                );
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
