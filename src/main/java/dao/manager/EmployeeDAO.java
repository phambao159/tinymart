package dao.manager;

import model.manager.employee.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;

public class EmployeeDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Helper: Chuyển đổi ResultSet sang Object Employee để tái sử dụng
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        return new Employee(
            rs.getInt("EmployeeID"),
            rs.getString("FullName"),
            rs.getDate("DateOfBirth") != null ? rs.getDate("DateOfBirth").toLocalDate() : null,
            rs.getString("PhoneNumber"),
            rs.getString("Address"),
            rs.getString("Role"),
            rs.getDate("HireDate") != null ? rs.getDate("HireDate").toLocalDate() : null,
            rs.getLong("BaseSalary"),
            rs.getString("User"),
            rs.getString("Password"),
            rs.getString("Status")
        );
    }

    /**
     * 1. ĐĂNG NHẬP: Chỉ cho phép nhân viên 'Active' đăng nhập
     */
    public Employee authenticate(String username, String password) throws Exception {
        String sql = "SELECT * FROM Employee WHERE User = ? AND Password = ? AND Status = 'Active'";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi truy vấn cơ sở dữ liệu khi đăng nhập.", e);
        }
        return null;
    }

    /**
     * 2. READ: Lấy danh sách nhân viên chưa bị xóa (Status != 'Inactive')
     */
    public List<Employee> getData() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM Employee WHERE Status != 'Inactive' ORDER BY EmployeeID DESC";
        try (Connection conn = dc.getConnect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getData Employee: " + e.getMessage());
        }
        return list;
    }

    /**
     * 3. SEARCH: Tìm kiếm theo Tên hoặc Số điện thoại
     */
    public List<Employee> search(String keyword) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM Employee WHERE (FullName LIKE ? OR PhoneNumber LIKE ?) AND Status != 'Inactive'";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEmployee(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi search Employee: " + e.getMessage());
        }
        return list;
    }

    /**
     * 4. CREATE: Thêm mới (Mặc định Status là 'Active')
     */
    public boolean insert(Employee emp) {
        String sql = "INSERT INTO Employee (FullName, DateOfBirth, PhoneNumber, Address, Role, HireDate, BaseSalary, User, Password, Status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active')";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emp.getFullName());
            pstmt.setDate(2, emp.getDateOfBirth() != null ? Date.valueOf(emp.getDateOfBirth()) : null);
            pstmt.setString(3, emp.getPhoneNumber());
            pstmt.setString(4, emp.getAddress());
            pstmt.setString(5, emp.getRole());
            pstmt.setDate(6, emp.getHireDate() != null ? Date.valueOf(emp.getHireDate()) : null);
            pstmt.setLong(7, emp.getBaseSalary());
            pstmt.setString(8, emp.getUser());
            pstmt.setString(9, emp.getPassword());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi insert Employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * 5. DELETE (SOFT): Chuyển trạng thái sang 'Inactive'
     */
    public boolean delete(int employeeID) {
        String sql = "UPDATE Employee SET Status = 'Inactive' WHERE EmployeeID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi delete (soft) Employee: " + e.getMessage());
            return false;
        }
    }
}