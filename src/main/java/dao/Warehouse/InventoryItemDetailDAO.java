package dao.Warehouse;

import model.Warehouse.InventoryItemDetailRow;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemDetailDAO {

    // Lấy tất cả expiryDate cho một ProductSizeID
    public List<InventoryItemDetailRow> getDetailsByProductSizeId(int productSizeId) throws SQLException {
        List<InventoryItemDetailRow> details = new ArrayList<>();

        String sql = "SELECT id.ExpiryDate, id.Quantity, id.ShelfQuantity "
                + "FROM ImportDetail id "
                + "JOIN Import i ON id.ImportID = i.ImportID "
                + "WHERE i.Status = 'Completed' AND id.ProductSizeID = ? "
                + "AND (id.Quantity > 0 OR id.ShelfQuantity > 0) "
                + // ✅ chỉ lấy lô còn hàng
                "ORDER BY id.ExpiryDate ASC";

        try (Connection conn = new DBConnection().getConnect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productSizeId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                details.add(new InventoryItemDetailRow(
                        rs.getDate("ExpiryDate") != null ? rs.getDate("ExpiryDate").toString() : "N/A",
                        rs.getInt("Quantity"),
                        rs.getInt("ShelfQuantity")
                ));
            }
        }

        return details;
    }

    // Cập nhật ShelfQuantity: trừ Quantity và cộng ShelfQuantity cho lô gần hết hạn nhất
    // Cập nhật ShelfQuantity: trừ Quantity và cộng ShelfQuantity cho lô gần hết hạn nhất
    public boolean updateShelfQuantity(int productSizeId, int amount) throws SQLException {
        String selectSql = "SELECT id.ImportDetailID, id.Quantity "
                + "FROM ImportDetail id "
                + "JOIN Import i ON id.ImportID = i.ImportID "
                + "WHERE id.ProductSizeID = ? AND id.Quantity > 0 AND i.Status = 'Completed' "
                + "ORDER BY id.ExpiryDate ASC LIMIT 1";

        try (Connection conn = new DBConnection().getConnect(); PreparedStatement ps = conn.prepareStatement(selectSql)) {

            ps.setInt(1, productSizeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int importDetailId = rs.getInt("ImportDetailID");
                int currentQty = rs.getInt("Quantity");

                // 🔍 Debug log
                System.out.println("[DEBUG] updateShelfQuantity:");
                System.out.println("  ProductSizeID = " + productSizeId);
                System.out.println("  ImportDetailID = " + importDetailId);
                System.out.println("  Current Quantity = " + currentQty);
                System.out.println("  Requested Amount = " + amount);

                if (amount > currentQty) {
                    System.out.println("  ❌ Requested amount > currentQty → return false");
                    return false;
                }

                String updateSql = "UPDATE ImportDetail "
                        + "SET Quantity = Quantity - ?, ShelfQuantity = ShelfQuantity + ? "
                        + "WHERE ImportDetailID = ?";
                try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                    ups.setInt(1, amount);
                    ups.setInt(2, amount);
                    ups.setInt(3, importDetailId);
                    int rows = ups.executeUpdate();

                    // 🔍 Debug log
                    System.out.println("  ✅ Update executed, rows affected = " + rows);
                }
                return true;
            } else {
                System.out.println("  ❌ No ImportDetail found for ProductSizeID = " + productSizeId);
            }
        }
        return false;
    }
}
