package dao.manager.employee;

import model.manager.employee.Shift;
import util.DBConnection;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShiftDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Get all shifts from the database
     */
    public List<Shift> getAllShifts() {
        List<Shift> list = new ArrayList<>();
        String sql = "SELECT * FROM Shift";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                // Converting SQL Time to Java LocalTime
                list.add(new Shift(
                    rs.getInt("ShiftID"),
                    rs.getTime("StartTime").toLocalTime(),
                    rs.getTime("EndTime").toLocalTime(),
                    rs.getString("ShiftName")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching shift list: " + e.getMessage());
        }
        return list;
    }

    /**
     * Add a new shift to the database
     */
    public boolean addShift(Shift shift) {
        String sql = "INSERT INTO Shift (StartTime, EndTime, ShiftName) VALUES (?, ?, ?)";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Converting LocalTime to SQL Time
            pstmt.setTime(1, Time.valueOf(shift.getStartTime()));
            pstmt.setTime(2, Time.valueOf(shift.getEndTime()));
            pstmt.setString(3, shift.getShiftName());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding new shift: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update an existing shift
     */
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
            System.err.println("Error updating shift ID " + shift.getShiftID() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a shift by its ID
     */
    public boolean deleteShift(int shiftID) {
        String sql = "DELETE FROM Shift WHERE ShiftID = ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, shiftID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting shift ID " + shiftID + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Search for shifts by name
     */
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
            System.err.println("Error searching for shift '" + name + "': " + e.getMessage());
        }
        return list;
    }
}