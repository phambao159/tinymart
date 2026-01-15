package dao.manager.supplier;

import model.manager.supplier.ImportDetail;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportDetailDAO {

    private final DBConnection dc = new DBConnection();

    // --- THÊM (ADD) ---
    public boolean addImportDetail(ImportDetail detail) {
        // Thêm cột ShelfQuantity vào SQL
        String sql = "INSERT INTO ImportDetail (ImportID, ProductSizeID, Quantity, ShelfQuantity, ImportPrice, ExpiryDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, detail.getImportID());
            pstmt.setInt(2, detail.getProductSizeID());
            pstmt.setLong(3, detail.getQuantity());
            pstmt.setInt(4, detail.getShelfQuantity()); // Gán số lượng trên kệ
            pstmt.setDouble(5, detail.getImportPrice());
            pstmt.setDate(6, detail.getExpiryDate() != null ? Date.valueOf(detail.getExpiryDate()) : null);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- SỬA (UPDATE) ---
    public boolean updateImportDetail(ImportDetail detail) {
        // Cập nhật thêm cột ShelfQuantity
        String sql = "UPDATE ImportDetail SET ProductSizeID = ?, Quantity = ?, ShelfQuantity = ?, ImportPrice = ?, ExpiryDate = ? WHERE ImportDetailID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, detail.getProductSizeID());
            pstmt.setLong(2, detail.getQuantity());
            pstmt.setInt(3, detail.getShelfQuantity()); // Cập nhật số lượng trên kệ
            pstmt.setDouble(4, detail.getImportPrice());
            pstmt.setDate(5, detail.getExpiryDate() != null ? Date.valueOf(detail.getExpiryDate()) : null);
            pstmt.setInt(6, detail.getImportDetailID());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- XÓA (DELETE) ---
    public boolean deleteImportDetail(int detailID) {
        String sql = "DELETE FROM ImportDetail WHERE ImportDetailID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, detailID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- LẤY DANH SÁCH THEO IMPORT ID ---
    public List<ImportDetail> getDetailsByImportID(int importID) {
        List<ImportDetail> list = new ArrayList<>();
        // Truy vấn id.* bao gồm cả ShelfQuantity đã có trong bảng
        String sql = "SELECT id.*, p.Name, p.ProductID, s.Type "
                + "FROM ImportDetail id "
                + "JOIN ProductSize ps ON id.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "WHERE id.ImportID = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, importID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ImportDetail detail = new ImportDetail();
                    detail.setImportDetailID(rs.getInt("ImportDetailID"));
                    detail.setImportID(rs.getInt("ImportID"));
                    detail.setProductID(rs.getInt("ProductID"));
                    detail.setProductSizeID(rs.getInt("ProductSizeID"));
                    detail.setQuantity(rs.getLong("Quantity"));
                    
                    // Lấy ShelfQuantity từ DB gán vào Model
                    detail.setShelfQuantity(rs.getInt("ShelfQuantity")); 
                    
                    detail.setImportPrice(rs.getDouble("ImportPrice"));

                    // Xử lý ngày hết hạn
                    if (rs.getDate("ExpiryDate") != null) {
                        detail.setExpiryDate(rs.getDate("ExpiryDate").toLocalDate());
                    }

                    // Thông tin bổ trợ hiển thị
                    detail.setProductName(rs.getString("Name"));
                    detail.setSizeName(rs.getString("Type"));

                    list.add(detail);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách chi tiết phiếu nhập: " + e.getMessage());
        }
        return list;
    }
}