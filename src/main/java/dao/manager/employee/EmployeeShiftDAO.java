package dao.manager.employee;

import model.manager.employee.EmployeeShift;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeShiftDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Fetches all shift assignments with Employee and Shift names.
     */
    public List<EmployeeShift> getAllAssignments() {
        List<EmployeeShift> list = new ArrayList<>();
        String sql = "SELECT es.*, e.FullName, s.ShiftName "
                + "FROM EmployeeShift es "
                + "JOIN Employee e ON es.EmployeeID = e.EmployeeID "
                + "JOIN Shift s ON es.ShiftID = s.ShiftID "
                + "ORDER BY es.WorkDate DESC, es.CheckInTime DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching shift assignments: " + e.getMessage());
        }
        return list;
    }

    /**
     * Inserts a new employee shift assignment.
     */
    public boolean addAssignment(EmployeeShift es) {
        String sql = "INSERT INTO EmployeeShift (EmployeeID, ShiftID, WorkDate, CheckInTime, CheckOutTime, StartCash, TotalSales, EndCash) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, es.getEmployeeID());
            pstmt.setInt(2, es.getShiftID());
            pstmt.setDate(3, Date.valueOf(es.getWorkDate()));
            pstmt.setTime(4, es.getCheckInTime() != null ? Time.valueOf(es.getCheckInTime()) : null);
            pstmt.setTime(5, es.getCheckOutTime() != null ? Time.valueOf(es.getCheckOutTime()) : null);
            pstmt.setBigDecimal(6, es.getStartCash());
            pstmt.setBigDecimal(7, es.getTotalSales());
            pstmt.setBigDecimal(8, es.getEndCash());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding shift assignment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing shift assignment using EmployeeShiftID.
     */
    public boolean updateAssignment(EmployeeShift es) {
        String sql = "UPDATE EmployeeShift SET EmployeeID = ?, ShiftID = ?, WorkDate = ?, "
                + "CheckInTime = ?, CheckOutTime = ?, StartCash = ?, TotalSales = ?, EndCash = ? "
                + "WHERE EmployeeShiftID = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, es.getEmployeeID());
            pstmt.setInt(2, es.getShiftID());
            pstmt.setDate(3, Date.valueOf(es.getWorkDate()));
            pstmt.setTime(4, es.getCheckInTime() != null ? Time.valueOf(es.getCheckInTime()) : null);
            pstmt.setTime(5, es.getCheckOutTime() != null ? Time.valueOf(es.getCheckOutTime()) : null);
            pstmt.setBigDecimal(6, es.getStartCash());
            pstmt.setBigDecimal(7, es.getTotalSales());
            pstmt.setBigDecimal(8, es.getEndCash());
            pstmt.setInt(9, es.getEmployeeShiftID());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating shift assignment ID " + es.getEmployeeShiftID() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a shift assignment by ID.
     */
    public boolean deleteAssignment(int employeeShiftID) {
        String sql = "DELETE FROM EmployeeShift WHERE EmployeeShiftID = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeShiftID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting shift assignment ID " + employeeShiftID + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Searches for assignments by Employee Name.
     */
    public List<EmployeeShift> searchByEmployeeName(String name) {
        List<EmployeeShift> list = new ArrayList<>();
        String sql = "SELECT es.*, e.FullName, s.ShiftName "
                + "FROM EmployeeShift es "
                + "JOIN Employee e ON es.EmployeeID = e.EmployeeID "
                + "JOIN Shift s ON es.ShiftID = s.ShiftID "
                + "WHERE e.FullName LIKE ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching assignments for: " + name);
        }
        return list;
    }

    /**
     * Helper method to map a ResultSet row to an EmployeeShift object.
     */
    private EmployeeShift mapResultSetToEntity(ResultSet rs) throws SQLException {
        EmployeeShift es = new EmployeeShift();
        es.setEmployeeShiftID(rs.getInt("EmployeeShiftID"));
        es.setEmployeeID(rs.getInt("EmployeeID"));
        es.setShiftID(rs.getInt("ShiftID"));
        es.setWorkDate(rs.getDate("WorkDate").toLocalDate());

        Time checkIn = rs.getTime("CheckInTime");
        if (checkIn != null) {
            es.setCheckInTime(checkIn.toLocalTime());
        }

        Time checkOut = rs.getTime("CheckOutTime");
        if (checkOut != null) {
            es.setCheckOutTime(checkOut.toLocalTime());
        }

        es.setStartCash(rs.getBigDecimal("StartCash"));
        es.setTotalSales(rs.getBigDecimal("TotalSales"));
        es.setEndCash(rs.getBigDecimal("EndCash"));

        // Extra display fields
        es.setEmployeeName(rs.getString("FullName"));
        es.setShiftName(rs.getString("ShiftName"));

        return es;
    }
}
