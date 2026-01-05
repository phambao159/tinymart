package dao.manager.supplier;

import model.manager.supplier.ImportDetail;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportDetailDAO {

    private final DBConnection dc = new DBConnection();

    // --- THÊM (ADD) ---
    // Thông thường việc thêm Detail sẽ nằm trong Transaction của ImportDAO 
    // Nhưng nếu bạn muốn thêm lẻ một món vào phiếu đã tồn tại:
    public boolean addImportDetail(ImportDetail detail) {
        String sql = "INSERT INTO ImportDetail (ImportID, ProductSizeID, Quantity, ImportPrice, ExpiryDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, detail.getImportID());
            pstmt.setInt(2, detail.getProductSizeID());
            pstmt.setLong(3, detail.getQuantity());
            pstmt.setDouble(4, detail.getImportPrice());
            pstmt.setDate(5, detail.getExpiryDate() != null ? Date.valueOf(detail.getExpiryDate()) : null);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- SỬA (UPDATE) ---
    public boolean updateImportDetail(ImportDetail detail) {
        String sql = "UPDATE ImportDetail SET ProductSizeID = ?, Quantity = ?, ImportPrice = ?, ExpiryDate = ? WHERE ImportDetailID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, detail.getProductSizeID());
            pstmt.setLong(2, detail.getQuantity());
            pstmt.setDouble(3, detail.getImportPrice());
            pstmt.setDate(4, detail.getExpiryDate() != null ? Date.valueOf(detail.getExpiryDate()) : null);
            pstmt.setInt(5, detail.getImportDetailID());

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

    public List<ImportDetail> getDetailsByImportID(int importID) {
        List<ImportDetail> list = new ArrayList<>();
        // JOIN để lấy được ProductName và SizeName từ ProductSizeID
        String sql = "SELECT id.*, p.Name,p.ProductID, s.Type "
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
                    detail.setProductID(rs.getInt("ProductID"));
                    detail.setImportDetailID(rs.getInt("ImportDetailID"));
                    detail.setImportID(rs.getInt("ImportID"));
                    detail.setProductSizeID(rs.getInt("ProductSizeID"));
                    detail.setQuantity(rs.getLong("Quantity"));
                    detail.setImportPrice(rs.getDouble("ImportPrice"));

                    // Xử lý ngày hết hạn
                    if (rs.getDate("ExpiryDate") != null) {
                        detail.setExpiryDate(rs.getDate("ExpiryDate").toLocalDate());
                    }

                    // Gán thêm thông tin bổ trợ để hiển thị trên TableView
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
