package dao.manager.product;

import model.manager.product.Promotion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.manager.product.ProductSize;
import util.DBConnection;

public class PromotionDAO {

    private final DBConnection dc = new DBConnection();

    // Lấy danh sách: Loại bỏ Inactive để giao diện sạch sẽ
    public List<Promotion> getData() {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM Promotion WHERE Status != 'Inactive' ORDER BY PromotionID DESC";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPromotion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Promotion> getProActive() {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM Promotion WHERE Status = 'Active' ORDER BY PromotionID DESC";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPromotion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Promotion p) {
        String sql = "INSERT INTO Promotion (Name, Description, Type, Value, StartDate, EndDate, Status) VALUES (?,?,?,?,?,?,?)";
        return executeUpdate(sql, p, false);
    }

    public boolean update(Promotion p) {
        String sql = "UPDATE Promotion SET Name=?, Description=?, Type=?, Value=?, StartDate=?, EndDate=?, Status=? WHERE PromotionID=?";
        return executeUpdate(sql, p, true);
    }

    public boolean delete(int id) {
        // Xóa mềm: Chuyển sang Inactive
        String sql = "UPDATE Promotion SET Status = 'Inactive' WHERE PromotionID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Promotion> searchByName(String keyword) {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM Promotion WHERE Name LIKE ? AND Status != 'Inactive'";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPromotion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Bổ sung hàm này để phục vụ trang Edit
    public Promotion getByID(int id) {
        String sql = "SELECT * FROM Promotion WHERE PromotionID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPromotion(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean executeUpdate(String sql, Promotion p, boolean isUpdate) {
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getType());
            ps.setDouble(4, p.getValue());
            // Tránh NullPointerException nếu ngày bị trống
            ps.setDate(5, p.getStartDate() != null ? Date.valueOf(p.getStartDate()) : null);
            ps.setDate(6, p.getEndDate() != null ? Date.valueOf(p.getEndDate()) : null);
            ps.setString(7, p.getStatus());

            if (isUpdate) {
                ps.setInt(8, p.getPromotionID());
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Promotion mapResultSetToPromotion(ResultSet rs) throws SQLException {
        return new Promotion(
                rs.getInt("PromotionID"),
                rs.getString("Name"),
                rs.getString("Description"),
                rs.getString("Type"),
                rs.getDouble("Value"),
                rs.getDate("StartDate") != null ? rs.getDate("StartDate").toLocalDate() : null,
                rs.getDate("EndDate") != null ? rs.getDate("EndDate").toLocalDate() : null,
                rs.getString("Status")
        );
    }

    public boolean applyPromotionToProducts(int promotionID, List<Integer> productSizeIDs) {
        String sqlClear = "UPDATE ProductSize SET PromotionID = NULL WHERE PromotionID = ?";
        String sqlApply = "UPDATE ProductSize SET PromotionID = ? WHERE ProductSizeID = ?";

        Connection conn = null;
        try {
            conn = dc.getConnect();
            conn.setAutoCommit(false); // Bắt đầu giao dịch

            // BƯỚC 1: Gỡ bỏ khuyến mãi này khỏi tất cả sản phẩm đang áp dụng nó
            try (PreparedStatement psClear = conn.prepareStatement(sqlClear)) {
                psClear.setInt(1, promotionID);
                psClear.executeUpdate();
            }

            // BƯỚC 2: Nếu danh sách ID mới có dữ liệu, tiến hành áp dụng cho các ID đó
            if (productSizeIDs != null && !productSizeIDs.isEmpty()) {
                try (PreparedStatement psApply = conn.prepareStatement(sqlApply)) {
                    for (Integer psID : productSizeIDs) {
                        psApply.setInt(1, promotionID);
                        psApply.setInt(2, psID);
                        psApply.addBatch();
                    }
                    psApply.executeBatch();
                }
            }

            conn.commit(); // Hoàn tất nếu mọi thứ trơn tru
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Quay xe nếu có lỗi xảy ra
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<ProductSize> getProductSizesByPromotion(int promotionID) {
        List<ProductSize> list = new ArrayList<>();
        // QUAN TRỌNG: Phải lấy cả ps.ProductID
        String sql = "SELECT ps.*, p.Name, s.Type, p.ProductID "
                + "FROM ProductSize ps "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "WHERE ps.PromotionID = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promotionID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductSize size = new ProductSize();
                size.setProductSizeID(rs.getInt("ProductSizeID"));
                size.setProductID(rs.getInt("ProductID")); // THÊM DÒNG NÀY
                size.setProductName(rs.getString("Name"));
                size.setSizeType(rs.getString("Type"));
                list.add(size);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void forceUpdateExpired() {
        String sql = "UPDATE Promotion SET Status = 'Expired' "
                + "WHERE EndDate < CURRENT_DATE AND Status NOT IN ('Expired', 'Inactive')";
        try (Connection conn = dc.getConnect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);

            // Gỡ luôn PromotionID ở ProductSize cho các khuyến mãi vừa hết hạn
            String sql2 = "UPDATE ProductSize ps JOIN Promotion p ON ps.PromotionID = p.PromotionID "
                    + "SET ps.PromotionID = NULL WHERE p.EndDate < CURRENT_DATE";
            stmt.executeUpdate(sql2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
