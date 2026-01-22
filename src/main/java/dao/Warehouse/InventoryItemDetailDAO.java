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

        String sql = "SELECT id.ExpiryDate, id.Quantity, id.ShelfQuantity " +
                     "FROM ImportDetail id " +
                     "JOIN Import i ON id.ImportID = i.ImportID " +
                     "WHERE i.Status = 'Completed' AND id.ProductSizeID = ? " +
                     "ORDER BY id.ExpiryDate ASC";

        Connection conn = new DBConnection().getConnect();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
}