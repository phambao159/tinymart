package dao.Warehouse;

import model.Warehouse.InventoryItemCard;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemCardDAO {

    // Lọc theo tên sản phẩm và tổng stock (Quantity + ShelfQuantity) chỉ từ Import Completed
    public List<InventoryItemCard> searchItems(String name, Integer stockMin, Integer stockMax) throws SQLException {
        List<InventoryItemCard> items = new ArrayList<>();

        // ✅ Query chính từ ProductSize, join Product và Size
        // Stock tính bằng subquery từ ImportDetail
        StringBuilder sql = new StringBuilder(
                "SELECT ps.ProductSizeID, p.Name, s.Type, p.Image, " +
                "       COALESCE((SELECT SUM(id.Quantity + id.ShelfQuantity) " +
                "                 FROM ImportDetail id " +
                "                 JOIN Import i ON id.ImportID = i.ImportID " +
                "                 WHERE i.Status = 'Completed' AND id.ProductSizeID = ps.ProductSizeID), 0) AS TotalStock " +
                "FROM ProductSize ps " +
                "JOIN Product p ON ps.ProductID = p.ProductID " +
                "JOIN Size s ON ps.SizeID = s.SizeID " +
                "WHERE 1=1 "
        );

        if (name != null && !name.isEmpty()) {
            sql.append("AND p.Name LIKE ? ");
        }

        sql.append("HAVING 1=1 "); // ✅ dùng HAVING để lọc theo stock

        if (stockMin != null) {
            sql.append("AND TotalStock >= ? ");
        }
        if (stockMax != null) {
            sql.append("AND TotalStock <= ? ");
        }

        System.out.println("Executing SQL: " + sql);

        Connection conn = new DBConnection().getConnect();
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (name != null && !name.isEmpty()) {
                stmt.setString(idx++, "%" + name + "%");
            }
            if (stockMin != null) {
                stmt.setInt(idx++, stockMin);
            }
            if (stockMax != null) {
                stmt.setInt(idx++, stockMax);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("DAO result: " + rs.getString("Name")
                        + " Stock=" + rs.getInt("TotalStock"));

                items.add(new InventoryItemCard(
                        rs.getInt("ProductSizeID"),
                        rs.getString("Name"),
                        rs.getString("Type"),
                        rs.getInt("TotalStock"),   // ✅ chỉ còn Stock
                        rs.getString("Image")
                ));
            }
        }
        System.out.println("DAO returned items: " + items.size());
        return items;
    }
}