package dao.Warehouse;

import model.Warehouse.InventoryReport;
import util.DBConnection;
import javafx.scene.image.Image;

import java.sql.*;

public class InventoryReportDAO {

    /** Lấy sản phẩm có ExpiryDate cận nhất */
    public InventoryReport getNearestExpireProduct() throws SQLException {
        String sql = "SELECT d.ImportDetailID, p.Name, s.Type AS SizeType, d.ExpiryDate, "
                + "d.Quantity, d.ShelfQuantity, p.Image "
                + "FROM ImportDetail d "
                + "JOIN ProductSize ps ON d.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "WHERE d.ExpiryDate IS NOT NULL "
                + "ORDER BY d.ExpiryDate ASC LIMIT 1";

        try (Connection conn = new DBConnection().getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int totalQty = rs.getInt("Quantity") + rs.getInt("ShelfQuantity");
                String status = totalQty < 40 ? "⚠ Low Stock" : "OK";

                Image img = loadImage(rs.getString("Image"));

                return new InventoryReport(
                        rs.getInt("ImportDetailID"),
                        rs.getString("Name"),
                        rs.getString("SizeType"),
                        rs.getString("ExpiryDate"),
                        "Nearest Expire",
                        totalQty,
                        status,
                        img,
                        "" // không cần updateTime
                );
            }
        }
        return null;
    }

    /** Lấy sản phẩm có stock thấp nhất */
    public InventoryReport getLowestStockProduct() throws SQLException {
        String sql = "SELECT d.ImportDetailID, p.Name, s.Type AS SizeType, d.ExpiryDate, "
                + "d.Quantity, d.ShelfQuantity, p.Image "
                + "FROM ImportDetail d "
                + "JOIN ProductSize ps ON d.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "ORDER BY (d.Quantity + d.ShelfQuantity) ASC LIMIT 1";

        try (Connection conn = new DBConnection().getConnect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int totalQty = rs.getInt("Quantity") + rs.getInt("ShelfQuantity");
                String status = totalQty < 40 ? "⚠ Low Stock" : "OK";

                Image img = loadImage(rs.getString("Image"));

                return new InventoryReport(
                        rs.getInt("ImportDetailID"),
                        rs.getString("Name"),
                        rs.getString("SizeType"),
                        rs.getString("ExpiryDate"),
                        "Lowest Stock",
                        totalQty,
                        status,
                        img,
                        "" // không cần updateTime
                );
            }
        }
        return null;
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