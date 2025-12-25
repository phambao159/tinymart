package dao.manager;

import model.manager.EmployeeShift;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeShiftDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Lấy toàn bộ danh sách phân công ca trực
     * Kết hợp JOIN để lấy FullName từ bảng Employee và ShiftName từ bảng Shift
     */
    public List<EmployeeShift> getAllAssignments() {
        List<EmployeeShift> list = new ArrayList<>();
        String sql = "SELECT es.EmployeeID, es.ShiftID, es.Date, e.FullName, s.ShiftName " +
                     "FROM EmployeeShift es " +
                     "JOIN Employee e ON es.EmployeeID = e.EmployeeID " +
                     "JOIN Shift s ON es.ShiftID = s.ShiftID " +
                     "ORDER BY es.Date DESC";

        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                EmployeeShift es = new EmployeeShift();
                es.setEmployeeID(rs.getInt("EmployeeID"));
                es.setShiftID(rs.getInt("ShiftID"));
                es.setDate(rs.getDate("Date").toLocalDate());
                
                // Các trường bổ trợ hiển thị
                es.setEmployeeName(rs.getString("FullName"));
                es.setShiftName(rs.getString("ShiftName"));
                
                list.add(es);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách phân ca: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm một phân công ca trực mới
     */
    public boolean assignShift(EmployeeShift es) {
        String sql = "INSERT INTO EmployeeShift (EmployeeID, ShiftID, Date) VALUES (?, ?, ?)";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, es.getEmployeeID());
            pstmt.setInt(2, es.getShiftID());
            pstmt.setDate(3, Date.valueOf(es.getDate()));
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Lỗi thường gặp: Nhân viên đã bị phân vào ca đó trong cùng ngày (Duplicate Primary Key)
            System.err.println("Lỗi khi phân ca (có thể đã tồn tại): " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa một phân công ca trực
     */
    public boolean deleteAssignment(int employeeID, int shiftID, Date date) {
        String sql = "DELETE FROM EmployeeShift WHERE EmployeeID = ? AND ShiftID = ? AND Date = ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, employeeID);
            pstmt.setInt(2, shiftID);
            pstmt.setDate(3, date);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa phân ca: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tìm kiếm phân ca theo tên nhân viên
     */
    public List<EmployeeShift> searchByEmployeeName(String name) {
        List<EmployeeShift> list = new ArrayList<>();
        String sql = "SELECT es.EmployeeID, es.ShiftID, es.Date, e.FullName, s.ShiftName " +
                     "FROM EmployeeShift es " +
                     "JOIN Employee e ON es.EmployeeID = e.EmployeeID " +
                     "JOIN Shift s ON es.ShiftID = s.ShiftID " +
                     "WHERE e.FullName LIKE ?";

        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                EmployeeShift es = new EmployeeShift();
                es.setEmployeeID(rs.getInt("EmployeeID"));
                es.setShiftID(rs.getInt("ShiftID"));
                es.setDate(rs.getDate("Date").toLocalDate());
                es.setEmployeeName(rs.getString("FullName"));
                es.setShiftName(rs.getString("ShiftName"));
                list.add(es);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}