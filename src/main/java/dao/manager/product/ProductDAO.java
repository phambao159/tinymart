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

    public List<ProductSummary> getProductSummaries(String keyword, String category, String size, String promotion) {
        List<ProductSummary> productSummaries = new ArrayList<>();

        // Cập nhật SQL: Lấy PromotionID từ bảng PS (ProductSize) thay vì bảng P (Product)
        StringBuilder sql = new StringBuilder(
                "SELECT P.ProductID, P.Name, P.CategoryID, P.Unit, P.Image, P.Status, "
                + "MIN(PS.SellingPrice) AS MinSellingPrice, "
                + "COALESCE(SUM(CASE WHEN I.Status = 'Completed' THEN ID.Quantity ELSE 0 END), 0) AS TotalStockQuantity "
                + "FROM Product P "
                + "LEFT JOIN ProductSize PS ON P.ProductID = PS.ProductID "
                + "LEFT JOIN ImportDetail ID ON PS.ProductSizeID = ID.ProductSizeID "
                + "LEFT JOIN Import I ON ID.ImportID = I.ImportID "
                + "LEFT JOIN Category C ON P.CategoryID = C.CategoryID "
                + "LEFT JOIN Size S ON PS.SizeID = S.SizeID "
                + "LEFT JOIN Promotion PR ON PS.PromotionID = PR.PromotionID " // JOIN qua bảng PS
                + "WHERE 1=1 "
        );

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND P.Name LIKE ? ");
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND C.Name = ? ");
        }
        if (size != null && !size.isEmpty()) {
            sql.append(" AND S.Type = ? ");
        }
        if (promotion != null && !promotion.isEmpty()) {
            sql.append(" AND PR.Name = ? ");
        }

        // Bỏ P.PromotionID khỏi GROUP BY vì nó không còn ở bảng Product
        sql.append(" GROUP BY P.ProductID, P.Name, P.CategoryID, P.Unit, P.Image, P.Status ");
        sql.append(" ORDER BY P.ProductID ");

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
            }
            if (category != null && !category.isEmpty()) {
                ps.setString(index++, category);
            }
            if (size != null && !size.isEmpty()) {
                ps.setString(index++, size);
            }
            if (promotion != null && !promotion.isEmpty()) {
                ps.setString(index++, promotion);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Sử dụng Constructor của ProductSummary đã lược bỏ PromotionID
                    productSummaries.add(new ProductSummary(
                            rs.getInt("ProductID"),
                            rs.getString("Name"),
                            rs.getInt("CategoryID"),
                            rs.getString("Unit"),
                            rs.getString("Image"),
                            rs.getDouble("MinSellingPrice"),
                            rs.getLong("TotalStockQuantity"),
                            rs.getString("Status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy ProductSummary: " + e.getMessage());
        }
        return productSummaries;
    }

    public boolean insert(Product p) {
        // Loại bỏ PromotionID khỏi câu lệnh INSERT của Product
        String sql = "INSERT INTO Product (Name, CategoryID, Unit, Image, Status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getCategoryID());
            ps.setString(3, p.getUnit());
            ps.setString(4, p.getImage());
            ps.setString(5, p.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Product p) {
        // Loại bỏ PromotionID khỏi câu lệnh UPDATE của Product
        String sql = "UPDATE Product SET Name=?, CategoryID=?, Unit=?, Image=?, Status=? WHERE ProductID=?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getCategoryID());
            ps.setString(3, p.getUnit());
            ps.setString(4, p.getImage());
            ps.setString(5, p.getStatus());
            ps.setInt(6, p.getProductID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int productId) {
        String sql = "UPDATE Product SET Status = 'Inactive' WHERE ProductID = ?";
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