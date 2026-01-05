package dao.manager.employee;

import model.manager.employee.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;

public class EmployeeDAO {

    DBConnection dc = new DBConnection();

    /**
     * Kiểm tra đăng nhập Trả về EmployeeID dưới dạng String nếu thành công,
     * null nếu thất bại
     */
    public Employee authenticate(String username, String password) throws Exception {
        String sql = "SELECT * FROM Employee WHERE User = ? AND Password = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setEmployeeID(rs.getInt("EmployeeID"));
                    emp.setFullName(rs.getString("FullName"));

                    // Chuyển đổi SQL Date sang LocalDate an toàn
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

                    return emp;
                }
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi truy vấn cơ sở dữ liệu khi đăng nhập.", e);
        }
        return null;
    }

    /**
     * Lấy toàn bộ danh sách nhân viên từ Database
     */
    public List<Employee> getData() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM Employee";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setEmployeeID(rs.getInt("EmployeeID"));
                emp.setFullName(rs.getString("FullName"));

                // Chuyển đổi SQL Date sang LocalDate an toàn
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

                list.add(emp);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getData Employee: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm mới nhân viên Phù hợp với AddEmployeeController
     */
    public boolean insert(Employee emp) {
        String sql = "INSERT INTO Employee (FullName, DateOfBirth, PhoneNumber, Address, Role, HireDate, BaseSalary, User, Password) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, emp.getFullName());

            // Xử lý LocalDate sang SQL Date (tránh NullPointerException)
            if (emp.getDateOfBirth() != null) {
                pstmt.setDate(2, Date.valueOf(emp.getDateOfBirth()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }

            pstmt.setString(3, emp.getPhoneNumber());
            pstmt.setString(4, emp.getAddress());
            pstmt.setString(5, emp.getRole());

            if (emp.getHireDate() != null) {
                pstmt.setDate(6, Date.valueOf(emp.getHireDate()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }

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
     * Xóa nhân viên theo ID
     */
    public boolean delete(int employeeID) {
        String sql = "DELETE FROM Employee WHERE EmployeeID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi delete Employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thông tin nhân viên (Gợi ý thêm cho chức năng Edit)
     */
    public boolean update(Employee emp) {
        String sql = "UPDATE Employee SET FullName=?, DateOfBirth=?, PhoneNumber=?, Address=?, Role=?, HireDate=?, BaseSalary=?, User=?, Password=? "
                + "WHERE EmployeeID=?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, emp.getFullName());
            pstmt.setDate(2, Date.valueOf(emp.getDateOfBirth()));
            pstmt.setString(3, emp.getPhoneNumber());
            pstmt.setString(4, emp.getAddress());
            pstmt.setString(5, emp.getRole());
            pstmt.setDate(6, Date.valueOf(emp.getHireDate()));
            pstmt.setLong(7, emp.getBaseSalary());
            pstmt.setString(8, emp.getUser());
            pstmt.setString(9, emp.getPassword());
            pstmt.setInt(10, emp.getEmployeeID());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi update Employee: " + e.getMessage());
            return false;
        }
    }

    public Employee getEmployeeById(int employeeID) {
        String sql = "SELECT * FROM Employee WHERE EmployeeID = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setEmployeeID(rs.getInt("EmployeeID"));
                    emp.setFullName(rs.getString("FullName"));

                    // Chuyển đổi Date an toàn
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

                    return emp;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy đối tượng Employee theo ID: " + e.getMessage());
        }
        return null;
    }
}
