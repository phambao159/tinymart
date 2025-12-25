package dao.manager;

import model.manager.Supplier;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Lấy toàn bộ danh sách nhà cung cấp
     */
    public List<Supplier> getAllSuppliers() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM Supplier";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new Supplier(
                    rs.getInt("SupplierID"),
                    rs.getString("Name"),
                    rs.getString("ContactPerson"),
                    rs.getString("PhoneNumber"),
                    rs.getString("Address")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách nhà cung cấp: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm nhà cung cấp mới
     */
    public boolean addSupplier(Supplier supplier) {
        String sql = "INSERT INTO Supplier (Name, ContactPerson, PhoneNumber, Address) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getPhoneNumber());
            pstmt.setString(4, supplier.getAddress());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm nhà cung cấp: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thông tin nhà cung cấp
     */
    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE Supplier SET Name = ?, ContactPerson = ?, PhoneNumber = ?, Address = ? WHERE SupplierID = ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getPhoneNumber());
            pstmt.setString(4, supplier.getAddress());
            pstmt.setInt(5, supplier.getSupplierID());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật nhà cung cấp: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa nhà cung cấp theo ID
     */
    public boolean deleteSupplier(int supplierID) {
        String sql = "DELETE FROM Supplier WHERE SupplierID = ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, supplierID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Lỗi có thể xảy ra nếu nhà cung cấp này đang có trong các bảng nhập hàng (Foreign Key Constraint)
            System.err.println("Lỗi khi xóa nhà cung cấp: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tìm kiếm nhà cung cấp theo tên hoặc số điện thoại
     */
    public List<Supplier> searchSuppliers(String keyword) {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM Supplier WHERE Name LIKE ? OR PhoneNumber LIKE ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String val = "%" + keyword + "%";
            pstmt.setString(1, val);
            pstmt.setString(2, val);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Supplier(
                        rs.getInt("SupplierID"),
                        rs.getString("Name"),
                        rs.getString("ContactPerson"),
                        rs.getString("PhoneNumber"),
                        rs.getString("Address")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm nhà cung cấp: " + e.getMessage());
        }
        return list;
    }
}