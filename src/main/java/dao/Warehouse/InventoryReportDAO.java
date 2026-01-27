package dao.Warehouse;

import model.Warehouse.InventoryReport;
import util.DBConnection;
import javafx.scene.image.Image;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryReportDAO {

    /** Lấy toàn bộ sản phẩm có ExpiryDate gần nhất (ví dụ trong vòng 30 ngày tới) */
    public List<InventoryReport> getAllNearestExpireProducts() throws SQLException {
        String sql = "SELECT d.ImportDetailID, p.Name, s.Type AS SizeType, d.ExpiryDate, " +
                     "d.Quantity, d.ShelfQuantity, p.Image " +
                     "FROM ImportDetail d " +
                     "JOIN ProductSize ps ON d.ProductSizeID = ps.ProductSizeID " +
                     "JOIN Product p ON ps.ProductID = p.ProductID " +
                     "JOIN Size s ON ps.SizeID = s.SizeID " +
                     "WHERE d.ExpiryDate IS NOT NULL " +
                     "AND d.ExpiryDate <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) " + // lấy các lô hàng hết hạn trong 30 ngày tới
                     "ORDER BY d.ExpiryDate ASC";

        List<InventoryReport> list = new ArrayList<>();
        try (Connection conn = new DBConnection().getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int totalQty = rs.getInt("Quantity") + rs.getInt("ShelfQuantity");
                String status = totalQty < 20 ? "⚠ Low Stock" : "";
                Image img = loadImage(rs.getString("Image"));

                list.add(new InventoryReport(
                        rs.getInt("ImportDetailID"),
                        rs.getString("Name"),
                        rs.getString("SizeType"),
                        rs.getString("ExpiryDate"),
                        "Nearest Expire",
                        totalQty,
                        status,
                        img,
                        ""
                ));
            }
        }
        return list;
    }

    /** Lấy toàn bộ sản phẩm có stock thấp (ví dụ dưới 20) */
    public List<InventoryReport> getAllLowStockProducts() throws SQLException {
        String sql = "SELECT d.ImportDetailID, p.Name, s.Type AS SizeType, d.ExpiryDate, " +
                     "d.Quantity, d.ShelfQuantity, p.Image " +
                     "FROM ImportDetail d " +
                     "JOIN ProductSize ps ON d.ProductSizeID = ps.ProductSizeID " +
                     "JOIN Product p ON ps.ProductID = p.ProductID " +
                     "JOIN Size s ON ps.SizeID = s.SizeID " +
                     "WHERE (d.Quantity + d.ShelfQuantity) < 20 " +
                     "ORDER BY (d.Quantity + d.ShelfQuantity) ASC";

        List<InventoryReport> list = new ArrayList<>();
        try (Connection conn = new DBConnection().getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int totalQty = rs.getInt("Quantity") + rs.getInt("ShelfQuantity");
                String status = totalQty < 20 ? "⚠ Low Stock" : "";
                Image img = loadImage(rs.getString("Image"));

                list.add(new InventoryReport(
                        rs.getInt("ImportDetailID"),
                        rs.getString("Name"),
                        rs.getString("SizeType"),
                        rs.getString("ExpiryDate"),
                        "Low Stock",
                        totalQty,
                        status,
                        img,
                        ""
                ));
            }
        }
        return list;
    }

    /** Helper: load ảnh từ resource */
    private Image loadImage(String imgFileName) {
        if (imgFileName != null && !imgFileName.isEmpty()) {
            try {
                String imagePath = "/image/manager/" + imgFileName;
                return new Image(getClass().getResourceAsStream(imagePath), 80, 80, true, true);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}