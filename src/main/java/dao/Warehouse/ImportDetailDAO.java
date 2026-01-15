package dao.Warehouse;

import model.Warehouse.ImportDetail;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ImportDetailDAO {

    private Connection conn;

    public ImportDetailDAO(Connection conn) {
        this.conn = conn;
    }

    // Lấy danh sách chi tiết theo ImportID (JOIN để lấy ProductName và SizeType)
    public List<ImportDetail> getDetailsByImportId(int importId) {
        List<ImportDetail> list = new ArrayList<>();
        String sql = "SELECT id.ImportDetailID, id.ImportID, id.ProductSizeID, " +
                     "id.Quantity, id.ImportPrice, id.ExpiryDate, " +
                     "p.Name AS ProductName, s.Type AS SizeType " +
                     "FROM ImportDetail id " +
                     "JOIN ProductSize ps ON id.ProductSizeID = ps.ProductSizeID " +
                     "JOIN Product p ON ps.ProductID = p.ProductID " +
                     "JOIN Size s ON ps.SizeID = s.SizeID " +
                     "WHERE id.ImportID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, importId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ImportDetail detail = new ImportDetail(
                        rs.getInt("ImportDetailID"),
                        rs.getInt("ImportID"),
                        rs.getInt("ProductSizeID"),
                        rs.getLong("Quantity"),
                        rs.getDouble("ImportPrice"),
                        rs.getDate("ExpiryDate") != null ? rs.getDate("ExpiryDate").toLocalDate() : null
                );
                detail.setProductName(rs.getString("ProductName"));
                detail.setSizeType(rs.getString("SizeType")); // lấy từ cột Type của bảng Size
                list.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy tên sản phẩm từ ProductSizeID
    public String getProductNameByProductSizeId(int productSizeId) {
        String sql = "SELECT p.Name " +
                     "FROM ProductSize ps " +
                     "JOIN Product p ON ps.ProductID = p.ProductID " +
                     "WHERE ps.ProductSizeID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productSizeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Lấy tên size (Type) từ ProductSizeID
    public String getSizeTypeByProductSizeId(int productSizeId) {
        String sql = "SELECT s.Type " +
                     "FROM ProductSize ps " +
                     "JOIN Size s ON ps.SizeID = s.SizeID " +
                     "WHERE ps.ProductSizeID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productSizeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Cập nhật ExpiryDate cho một ImportDetail
    public boolean updateExpiryDate(int importDetailID, LocalDate expiryDate) {
        String sql = "UPDATE ImportDetail SET ExpiryDate = ? WHERE ImportDetailID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(expiryDate));
            ps.setInt(2, importDetailID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}