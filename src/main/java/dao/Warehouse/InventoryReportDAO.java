package dao.Warehouse;

import model.Warehouse.InventoryReport;
import util.DBConnection;
import javafx.scene.image.Image;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryReportDAO {

    /**
     * Lấy sản phẩm có ExpiryDate cận nhất
     */
    public InventoryReport getNearestExpireProduct() throws SQLException {
        Connection conn = new DBConnection().getConnect();

        String sql = "SELECT d.ImportDetailID, p.Name, s.Type AS SizeType, d.ExpiryDate, "
                + "d.Quantity, d.ShelfQuantity, p.Image "
                + "FROM ImportDetail d "
                + "JOIN ProductSize ps ON d.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "WHERE d.ExpiryDate IS NOT NULL "
                + "ORDER BY d.ExpiryDate ASC LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int totalQty = rs.getInt("Quantity") + rs.getInt("ShelfQuantity");
                String status = totalQty < 40 ? "⚠ Low Stock" : "OK";

                Image img = null;
                String imgFileName = rs.getString("Image"); // ví dụ: "cola.png"
                if (imgFileName != null && !imgFileName.isEmpty()) {
                    try {
                        String imagePath = "/image/manager/" + imgFileName;
                        img = new Image(getClass().getResourceAsStream(imagePath), 80, 80, true, true);
                    } catch (Exception e) {
                        img = null;
                    }
                }

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

    /**
     * Lấy sản phẩm có stock (Quantity + ShelfQuantity) thấp nhất
     */
    public InventoryReport getLowestStockProduct() throws SQLException {
        Connection conn = new DBConnection().getConnect();

        String sql = "SELECT d.ImportDetailID, p.Name, s.Type AS SizeType, d.ExpiryDate, "
                + "d.Quantity, d.ShelfQuantity, p.Image "
                + "FROM ImportDetail d "
                + "JOIN ProductSize ps ON d.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "ORDER BY (d.Quantity + d.ShelfQuantity) ASC LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int totalQty = rs.getInt("Quantity") + rs.getInt("ShelfQuantity");
                String status = totalQty < 40 ? "⚠ Low Stock" : "OK";

                Image img = null;
                String imgFileName = rs.getString("Image"); // ví dụ: "cola.png"
                if (imgFileName != null && !imgFileName.isEmpty()) {
                    try {
                        String imagePath = "/image/manager/" + imgFileName;
                        img = new Image(getClass().getResourceAsStream(imagePath), 80, 80, true, true);
                    } catch (Exception e) {
                        img = null;
                    }
                }

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

    /**
     * Lấy danh sách logs từ bảng InventoryHistory (Import / Auto-Replenish)
     */
    public List<InventoryReport> getLogs(String filter) throws SQLException {
        List<InventoryReport> list = new ArrayList<>();
        Connection conn = new DBConnection().getConnect();

        String sql = "SELECT h.HistoryID, p.Name, s.Type AS SizeType, h.ExpireDate, "
                + "h.ActionType, h.UpdatedQuantity, h.UpdateTime, p.Image "
                + "FROM InventoryHistory h "
                + "JOIN ProductSize ps ON h.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID";

        if (filter != null && !filter.equalsIgnoreCase("All")) {
            sql += " WHERE h.ActionType = ?";
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (filter != null && !filter.equalsIgnoreCase("All")) {
                stmt.setString(1, filter);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Image img = null;
                String imgPath = rs.getString("Image");
                if (imgPath != null && !imgPath.isEmpty()) {
                    try {
                        img = new Image(imgPath, 80, 80, true, true);
                    } catch (Exception e) {
                        img = null;
                    }
                }

                InventoryReport report = new InventoryReport(
                        rs.getInt("HistoryID"),
                        rs.getString("Name"),
                        rs.getString("SizeType"),
                        rs.getString("ExpireDate"),
                        rs.getString("ActionType"),
                        rs.getInt("UpdatedQuantity"),
                        "", // Logs không cần status
                        img,
                        rs.getString("UpdateTime")
                );
                list.add(report);
            }
        }
        return list;
    }
}
