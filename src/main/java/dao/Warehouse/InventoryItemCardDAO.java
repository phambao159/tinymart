package dao.Warehouse;

import model.Warehouse.InventoryItemCard;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemCardDAO {

    // Lọc theo tên sản phẩm và tổng stock (Inbound+Outbound) chỉ từ Import Confirmed
    public List<InventoryItemCard> searchItems(String name, Integer stockMin, Integer stockMax) throws SQLException {
        List<InventoryItemCard> items = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT ps.ProductSizeID, p.Name, s.Type, p.Status, "
                + "SUM(id.Quantity) AS TotalInbound, "
                + "SUM(id.ShelfQuantity) AS TotalOutbound, "
                + "MAX(id.ExpiryDate) AS LatestExpiryDate, p.Image "
                + "FROM ImportDetail id "
                + "JOIN Import i ON id.ImportID = i.ImportID "
                + "JOIN ProductSize ps ON id.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "WHERE i.Status = 'Confirmed' "
        );

        if (name != null && !name.isEmpty()) {
            sql.append("AND p.Name LIKE ? ");
        }

        sql.append("GROUP BY ps.ProductSizeID, p.Name, s.Type, p.Status, p.Image HAVING 1=1 ");

        if (stockMin != null) {
            sql.append("AND (SUM(id.Quantity) + SUM(id.ShelfQuantity)) >= ? ");
        }
        if (stockMax != null) {
            sql.append("AND (SUM(id.Quantity) + SUM(id.ShelfQuantity)) <= ? ");
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
                        + " Inbound=" + rs.getInt("TotalInbound")
                        + " Outbound=" + rs.getInt("TotalOutbound"));
                items.add(new InventoryItemCard(
                        rs.getInt("ProductSizeID"),
                        rs.getString("Name"),
                        rs.getString("Type"),
                        rs.getString("Status"),
                        rs.getDate("LatestExpiryDate") != null ? rs.getDate("LatestExpiryDate").toLocalDate() : null,
                        rs.getInt("TotalInbound"),
                        rs.getInt("TotalOutbound"),
                        rs.getString("Image")
                ));
            }
        }
        System.out.println("DAO returned items: " + items.size());
        return items;
    }
}
