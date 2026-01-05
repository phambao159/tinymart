package dao.manager.product;

import model.manager.product.ProductSummary;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.manager.product.Product;
import util.DBConnection;

public class ProductDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Lấy danh sách sản phẩm kèm theo các bộ lọc động
     */
    public List<ProductSummary> getProductSummaries(String keyword, String category, String size, String promotion) {
        List<ProductSummary> productSummaries = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT P.ProductID, P.Name, P.CategoryID, P.Unit, P.Image, P.Status, "
                + "MIN(PS.CostPrice) AS MinCostPrice, "
                + "MIN(PS.SellingPrice) AS MinSellingPrice, "
                + "SUM(PS.StockQuantity) AS TotalStockQuantity "
                + "FROM Product P "
                + "LEFT JOIN ProductSize PS ON P.ProductID = PS.ProductID "
                + "LEFT JOIN Category C ON P.CategoryID = C.CategoryID "
                + "LEFT JOIN Size S ON PS.SizeID = S.SizeID "
                + "LEFT JOIN Promotion PR ON P.PromotionID = PR.PromotionID "
                + "WHERE 1=1 "
        );

        if (keyword != null && !keyword.isEmpty()) sql.append(" AND P.Name LIKE ?");
        if (category != null && !category.isEmpty()) sql.append(" AND C.Name = ?");
        if (size != null && !size.isEmpty()) sql.append(" AND S.Type = ?");
        if (promotion != null && !promotion.isEmpty()) sql.append(" AND PR.Name = ?");

        sql.append(" GROUP BY P.ProductID, P.Name, P.CategoryID, P.Unit, P.Image, P.Status ");
        sql.append(" ORDER BY P.ProductID");

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (keyword != null && !keyword.isEmpty()) ps.setString(paramIndex++, "%" + keyword + "%");
            if (category != null && !category.isEmpty()) ps.setString(paramIndex++, category);
            if (size != null && !size.isEmpty()) ps.setString(paramIndex++, size);
            if (promotion != null && !promotion.isEmpty()) ps.setString(paramIndex++, promotion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double minSellingPrice = rs.getDouble("MinSellingPrice");
                    long totalStockQuantity = rs.getLong("TotalStockQuantity");

                    if (rs.wasNull()) {
                        minSellingPrice = 0.0;
                        totalStockQuantity = 0L;
                    }

                    productSummaries.add(new ProductSummary(
                            rs.getInt("ProductID"),
                            rs.getString("Name"),
                            rs.getInt("CategoryID"),
                            rs.getString("Unit"),
                            rs.getString("Image"),
                            minSellingPrice,
                            totalStockQuantity,
                            rs.getString("Status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productSummaries;
    }

    /**
     * Thêm mới sản phẩm
     */
    public boolean insert(Product p) {
        // Chú ý: Thêm PromotionID vào câu lệnh INSERT
        String sql = "INSERT INTO Product (Name, CategoryID, Unit, Image, Status, PromotionID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getCategoryID());
            ps.setString(3, p.getUnit());
            ps.setString(4, p.getImage());
            ps.setString(5, p.getStatus());
            
            // Xử lý nếu PromotionID có thể null
            if (p.getPromotionID() > 0) {
                ps.setInt(6, p.getPromotionID());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    public boolean update(Product p) {
        String sql = "UPDATE Product SET Name=?, CategoryID=?, Unit=?, Image=?, Status=?, PromotionID=? WHERE ProductID=?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getCategoryID());
            ps.setString(3, p.getUnit());
            ps.setString(4, p.getImage());
            ps.setString(5, p.getStatus());
            
            if (p.getPromotionID() > 0) {
                ps.setInt(6, p.getPromotionID());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            
            ps.setInt(7, p.getProductID());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa sản phẩm
     */
    public boolean delete(int productId) {
        // Lưu ý: Cần cân nhắc việc xóa ràng buộc ở bảng ProductSize trước nếu DB không để Cascade Delete
        String sql = "DELETE FROM Product WHERE ProductID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ProductSummary> getProductSummaries() {
        return getProductSummaries(null, null, null, null);
    }
}