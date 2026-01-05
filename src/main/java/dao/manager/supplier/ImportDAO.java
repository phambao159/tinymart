package dao.manager.supplier;

import model.manager.supplier.Import;
import model.manager.supplier.ImportDetail;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportDAO {

    private final DBConnection dc = new DBConnection();

    // 1. Lấy toàn bộ danh sách phiếu nhập (Cập nhật Status)
    public List<Import> getAllImports() {
        List<Import> list = new ArrayList<>();
        String sql = "SELECT * FROM `Import` ORDER BY ImportID DESC";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Import(
                        rs.getInt("ImportID"),
                        rs.getInt("SupplierID"),
                        rs.getTimestamp("ReceiptDate").toLocalDateTime(),
                        rs.getInt("EmployeeID"),
                        rs.getDouble("TotalCost"),
                        rs.getString("Status") // Lấy cột Status từ DB thay vì để trống
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Lưu phiếu nhập và chi tiết (Cập nhật Status vào INSERT)
    public boolean saveImport(Import imp, List<ImportDetail> details) {
        Connection conn = null;
        // Bổ sung Status vào câu lệnh SQL
        String sqlImport = "INSERT INTO `Import` (SupplierID, ReceiptDate, EmployeeID, TotalCost, Status) VALUES (?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO `ImportDetail` (ImportID, ProductSizeID, Quantity, ImportPrice, ExpiryDate) VALUES (?, ?, ?, ?, ?)";

        try {
            conn = dc.getConnect();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            int importID = -1;
            try (PreparedStatement psImp = conn.prepareStatement(sqlImport, Statement.RETURN_GENERATED_KEYS)) {
                psImp.setInt(1, imp.getSupplierID());
                psImp.setTimestamp(2, Timestamp.valueOf(imp.getReceiptDate()));
                psImp.setInt(3, imp.getEmployeeID());
                psImp.setDouble(4, imp.getTotalCost());
                psImp.setString(5, imp.getStatus()); // Lưu giá trị Status từ Model

                psImp.executeUpdate();

                ResultSet rs = psImp.getGeneratedKeys();
                if (rs.next()) {
                    importID = rs.getInt(1);
                }
            }

            if (importID == -1) {
                throw new SQLException("Failed to get ImportID.");
            }

            // Lưu danh sách chi tiết (Sử dụng Batch Processing để tối ưu hiệu năng)
            try (PreparedStatement psDet = conn.prepareStatement(sqlDetail)) {
                for (ImportDetail item : details) {
                    psDet.setInt(1, importID);
                    psDet.setInt(2, item.getProductSizeID());
                    psDet.setLong(3, item.getQuantity());
                    psDet.setDouble(4, item.getImportPrice());
                    if (item.getExpiryDate() != null) {
                        psDet.setDate(5, Date.valueOf(item.getExpiryDate()));
                    } else {
                        psDet.setNull(5, Types.DATE);
                    }
                    psDet.addBatch();
                }
                psDet.executeBatch();
            }

            conn.commit(); // Thành công thì commit toàn bộ
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 4. Cập nhật phiếu nhập (Dùng Transaction)
    public boolean updateImport(Import imp, List<ImportDetail> details) {
        Connection conn = null;
        String sqlUpdateImport = "UPDATE `Import` SET SupplierID = ?, ReceiptDate = ?, EmployeeID = ?, TotalCost = ?, Status = ? WHERE ImportID = ?";
        String sqlDeleteDetails = "DELETE FROM `ImportDetail` WHERE ImportID = ?";
        String sqlInsertDetail = "INSERT INTO `ImportDetail` (ImportID, ProductSizeID, Quantity, ImportPrice, ExpiryDate) VALUES (?, ?, ?, ?, ?)";

        try {
            conn = dc.getConnect();
            conn.setAutoCommit(false); // Bắt đầu giao dịch

            // Bước 1: Cập nhật bảng Import
            try (PreparedStatement psImp = conn.prepareStatement(sqlUpdateImport)) {
                psImp.setInt(1, imp.getSupplierID());
                psImp.setTimestamp(2, Timestamp.valueOf(imp.getReceiptDate()));
                psImp.setInt(3, imp.getEmployeeID());
                psImp.setDouble(4, imp.getTotalCost());
                psImp.setString(5, imp.getStatus());
                psImp.setInt(6, imp.getImportID());
                psImp.executeUpdate();
            }

            // Bước 2: Xóa các chi tiết cũ
            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteDetails)) {
                psDel.setInt(1, imp.getImportID());
                psDel.executeUpdate();
            }

            // Bước 3: Chèn lại danh sách chi tiết mới (Batch Processing)
            try (PreparedStatement psDet = conn.prepareStatement(sqlInsertDetail)) {
                for (ImportDetail item : details) {
                    psDet.setInt(1, imp.getImportID());
                    psDet.setInt(2, item.getProductSizeID());
                    psDet.setLong(3, item.getQuantity());
                    psDet.setDouble(4, item.getImportPrice());
                    if (item.getExpiryDate() != null) {
                        psDet.setDate(5, Date.valueOf(item.getExpiryDate()));
                    } else {
                        psDet.setNull(5, Types.DATE);
                    }
                    psDet.addBatch();
                }
                psDet.executeBatch();
            }

            conn.commit(); // Hoàn tất giao dịch
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Lỗi thì khôi phục lại dữ liệu cũ
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 3. Xóa phiếu nhập
    public boolean deleteImport(int importID) {
        // Lưu ý: Nếu DB không dùng ON DELETE CASCADE, bạn cần xóa ImportDetail trước
        String sql = "DELETE FROM `Import` WHERE ImportID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, importID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
