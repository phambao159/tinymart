package dao.manager.customer;

import model.manager.customer.Customer;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private final DBConnection dc = new DBConnection();

    // 1. Lấy toàn bộ danh sách khách hàng
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM Customer ORDER BY CustomerID DESC";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Thêm khách hàng mới
    public boolean addCustomer(Customer c) {
        String sql = "INSERT INTO Customer (FullName, PhoneNumber, Points, Email, RegistrationDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getFullName());
            pstmt.setString(2, c.getPhoneNumber());
            pstmt.setInt(3, c.getPoints());
            pstmt.setString(4, c.getEmail());
            pstmt.setDate(5, Date.valueOf(c.getRegistrationDate()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Cập nhật thông tin khách hàng
    public boolean updateCustomer(Customer c) {
        String sql = "UPDATE Customer SET FullName = ?, PhoneNumber = ?, Points = ?, Email = ?, RegistrationDate = ? WHERE CustomerID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getFullName());
            pstmt.setString(2, c.getPhoneNumber());
            pstmt.setInt(3, c.getPoints());
            pstmt.setString(4, c.getEmail());
            pstmt.setDate(5, Date.valueOf(c.getRegistrationDate()));
            pstmt.setInt(6, c.getCustomerID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. Tìm kiếm khách hàng (Dùng cho chức năng Search nâng cao nếu không muốn dùng Filter list)
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM Customer WHERE FullName LIKE ? OR PhoneNumber LIKE ? OR Email LIKE ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchStr = "%" + keyword + "%";
            pstmt.setString(1, searchStr);
            pstmt.setString(2, searchStr);
            pstmt.setString(3, searchStr);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Hàm phụ trợ để chuyển đổi ResultSet sang Object Customer (giúp code sạch hơn)
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("CustomerID"),
                rs.getString("FullName"),
                rs.getString("PhoneNumber"),
                rs.getInt("Points"),
                rs.getString("Email"),
                rs.getDate("RegistrationDate").toLocalDate()
        );
    }

    public boolean isPhoneExists(String phone) {
        String sql = "SELECT COUNT(*) FROM customer WHERE phone = ?";

        // Sử dụng Try-with-resources để tự động đóng kết nối
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Nếu count > 0 nghĩa là đã tồn tại
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking phone existence: " + e.getMessage());
        }
        return false;
    }

    public boolean isPhoneExistsExcludeId(String phone, int currentId) {
        String sql = "SELECT COUNT(*) FROM customer WHERE phone = ? AND customerID != ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setInt(2, currentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
