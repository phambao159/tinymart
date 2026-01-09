package dao.Warehouse;

import model.Warehouse.Imports;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportDAO {

    private Connection conn;

    public ImportDAO(Connection conn) {
        this.conn = conn;
    }

    // Lấy tất cả Import
    public List<Imports> getAllImports() {
        List<Imports> list = new ArrayList<>();
        String sql = "SELECT * FROM Import";

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Imports imp = new Imports();
                imp.setImportID(rs.getInt("importID"));
                imp.setSupplierID(rs.getInt("supplierID"));
                imp.setEmployeeID(rs.getInt("employeeID"));
                imp.setReceiptDate(rs.getDate("receiptDate").toLocalDate());
                imp.setTotalCost(rs.getDouble("totalCost"));
                imp.setStatus(rs.getString("status"));

                // Gọi hàm lấy tên từ bảng khác
                imp.setSupplierName(getSupplierNameById(imp.getSupplierID()));
                imp.setEmployeeName(getEmployeeNameById(imp.getEmployeeID()));

                list.add(imp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy tên Supplier theo ID
    public String getSupplierNameById(int supplierID) {
        String sql = "SELECT name FROM Supplier WHERE supplierID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Supplier";
    }

    // Lấy tên Employee theo ID
    public String getEmployeeNameById(int employeeID) {
        String sql = "SELECT fullName FROM Employee WHERE employeeID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("fullName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Employee";
    }

    public boolean updateStatus(int importID, String newStatus) {
        String sql = "UPDATE Import SET Status = ? WHERE ImportID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, importID);
            int rows = ps.executeUpdate();
            return rows > 0; // true nếu cập nhật thành công
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

