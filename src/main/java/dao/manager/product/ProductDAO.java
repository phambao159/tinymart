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

    public List<ProductSummary> getProductSummaries(String keyword, String category, String size, String promotion, String status) {
        List<ProductSummary> productSummaries = new ArrayList<>();

        // 1. Khởi tạo StringBuilder SQL
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
                + "LEFT JOIN Promotion PR ON PS.PromotionID = PR.PromotionID "
                + "WHERE 1=1 "
        );

        // 2. Thêm các điều kiện lọc động
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
        // Lọc theo trạng thái (Active/Inactive)
        if (status != null && !status.isEmpty()) {
            sql.append(" AND P.Status = ? ");
        }

        // 3. Group By và Order By
        sql.append(" GROUP BY P.ProductID, P.Name, P.CategoryID, P.Unit, P.Image, P.Status ");
        sql.append(" ORDER BY P.ProductID DESC");

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            // 4. Set tham số cho PreparedStatement theo đúng thứ tự
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
            if (status != null && !status.isEmpty()) {
                ps.setString(index++, status);
            }

            // 5. Thực thi và map dữ liệu
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
        return getProductSummaries(null, null, null, null, null);
    }

    public boolean isNameExists(String name) {
        String sql = "SELECT COUNT(*) FROM Product WHERE name = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean isNameExistsForEdit(String name, int currentID) {
    // Câu lệnh SQL này tìm xem có sản phẩm NÀO KHÁC (ID khác) đang dùng tên này không
    String sql = "SELECT COUNT(*) FROM Product WHERE name = ? AND productID != ?";
    try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, name);
        ps.setInt(2, currentID); // Truyền ID của sản phẩm đang sửa vào đây
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
}
