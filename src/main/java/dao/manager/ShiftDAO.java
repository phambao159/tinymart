package dao.manager;

import model.manager.Shift;
import util.DBConnection;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShiftDAO {

    private final DBConnection dc = new DBConnection();

    // Lấy toàn bộ danh sách ca làm việc
    public List<Shift> getAllShifts() {
        List<Shift> list = new ArrayList<>();
        String sql = "SELECT * FROM Shift";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                // Sử dụng getTime().toLocalTime() để chuyển từ SQL Time sang Java LocalTime
                list.add(new Shift(
                    rs.getInt("ShiftID"),
                    rs.getTime("StartTime").toLocalTime(),
                    rs.getTime("EndTime").toLocalTime(),
                    rs.getString("ShiftName")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách ca trực: " + e.getMessage());
        }
        return list;
    }

    // Thêm một ca trực mới
    public boolean addShift(Shift shift) {
        String sql = "INSERT INTO Shift (StartTime, EndTime, ShiftName) VALUES (?, ?, ?)";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Chuyển LocalTime sang SQL Time
            pstmt.setTime(1, Time.valueOf(shift.getStartTime()));
            pstmt.setTime(2, Time.valueOf(shift.getEndTime()));
            pstmt.setString(3, shift.getShiftName());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm ca trực: " + e.getMessage());
            return false;
        }
    }

    // Cập nhật thông tin ca trực
    public boolean updateShift(Shift shift) {
        String sql = "UPDATE Shift SET StartTime = ?, EndTime = ?, ShiftName = ? WHERE ShiftID = ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTime(1, Time.valueOf(shift.getStartTime()));
            pstmt.setTime(2, Time.valueOf(shift.getEndTime()));
            pstmt.setString(3, shift.getShiftName());
            pstmt.setInt(4, shift.getShiftID());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật ca trực: " + e.getMessage());
            return false;
        }
    }

    // Xóa ca trực theo ID
    public boolean deleteShift(int shiftID) {
        String sql = "DELETE FROM Shift WHERE ShiftID = ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, shiftID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa ca trực: " + e.getMessage());
            return false;
        }
    }

    // Tìm kiếm ca trực theo tên
    public List<Shift> searchByShiftName(String name) {
        List<Shift> list = new ArrayList<>();
        String sql = "SELECT * FROM Shift WHERE ShiftName LIKE ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Shift(
                        rs.getInt("ShiftID"),
                        rs.getTime("StartTime").toLocalTime(),
                        rs.getTime("EndTime").toLocalTime(),
                        rs.getString("ShiftName")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm ca trực: " + e.getMessage());
        }
        return list;
    }
}