package dao.manager.employee;

import model.manager.employee.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;

public class EmployeeDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Helper để map dữ liệu từ ResultSet sang Object Employee
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setEmployeeID(rs.getInt("EmployeeID"));
        emp.setFullName(rs.getString("FullName"));
        if (rs.getDate("DateOfBirth") != null) {
            emp.setDateOfBirth(rs.getDate("DateOfBirth").toLocalDate());
        }
        emp.setPhoneNumber(rs.getString("PhoneNumber"));
        emp.setAddress(rs.getString("Address"));
        emp.setRole(rs.getString("Role"));
        if (rs.getDate("HireDate") != null) {
            emp.setHireDate(rs.getDate("HireDate").toLocalDate());
        }
        emp.setBaseSalary(rs.getLong("BaseSalary"));
        emp.setUser(rs.getString("User"));
        emp.setPassword(rs.getString("Password"));
        emp.setStatus(rs.getString("Status"));
        return emp;
    }

    /**
     * Kiểm tra đăng nhập: Chỉ cho phép nhân viên đang 'Active' đăng nhập
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
     * Lấy toàn bộ danh sách nhân viên chưa bị xóa (Status != 'Inactive')
     */
    public List<Employee> getData() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM Employee WHERE Status != 'Inactive' ORDER BY EmployeeID DESC";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getData Employee: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm mới nhân viên: Mặc định gán Status là 'Active'
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
     * Xóa mềm nhân viên: Cập nhật Status thành 'Inactive'
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

    /**
     * Cập nhật thông tin nhân viên
     */
    public boolean update(Employee emp) {
        String sql = "UPDATE Employee SET FullName=?, DateOfBirth=?, PhoneNumber=?, Address=?, Role=?, HireDate=?, BaseSalary=?, User=?, Password=?, Status=? "
                + "WHERE EmployeeID=?";
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
            pstmt.setString(10, emp.getStatus());
            pstmt.setInt(11, emp.getEmployeeID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi update Employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tìm kiếm theo Tên hoặc Số điện thoại (Chỉ tìm những người chưa bị xóa)
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

    public Employee getEmployeeById(int employeeID) {
        String sql = "SELECT * FROM Employee WHERE EmployeeID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getEmployeeById: " + e.getMessage());
        }
        return null;
    }

    public List<Employee> getCashiersOnly() {
        List<Employee> list = new ArrayList<>();
        // Giả sử bảng Employee có cột Role
        String sql = "SELECT * FROM Employee WHERE Role = 'Cashier' AND Status = 'Active'";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               list.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
