package dao.cashier;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import util.DBConnection;

public class EmployeeShiftDAO {

    private final DBConnection dc = new DBConnection();

    public int getCurrentShiftID() {
        int shiftId = 1;
        String sql = "SELECT ShiftID, StartTime, EndTime FROM Shift";
        
        try (Connection conn = dc.getConnect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {

            LocalTime now = LocalTime.now();
            
            while (rs.next()) {
                Time dbStart = rs.getTime("StartTime");
                Time dbEnd = rs.getTime("EndTime");
                int id = rs.getInt("ShiftID");
                
                if (dbStart != null && dbEnd != null) {
                    LocalTime start = dbStart.toLocalTime();
                    LocalTime end = dbEnd.toLocalTime();                                       
                    if (start.isBefore(end)) {
                        if ((now.equals(start) || now.isAfter(start)) && now.isBefore(end)) {
                            return id;
                        }
                    } 
                    else {
                        if ((now.equals(start) || now.isAfter(start)) || now.isBefore(end)) {
                            return id;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shiftId;
    }

    public boolean isCheckedIn(int employeeID, int shiftID) {
        String sql = "SELECT CheckInTime FROM EmployeeShift WHERE EmployeeID = ? AND ShiftID = ? AND WorkDate = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeID);
            pstmt.setInt(2, shiftID);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getTime("CheckInTime") != null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkIn(int employeeID, int shiftID, double startCash) {
        if (isCheckedIn(employeeID, shiftID)) {
            return false;
        }

        String sql = "UPDATE EmployeeShift SET CheckInTime = ?, StartCash = ? "
                + "WHERE EmployeeID = ? AND ShiftID = ? AND WorkDate = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTime(1, Time.valueOf(LocalTime.now()));
            pstmt.setDouble(2, startCash);

            pstmt.setInt(3, employeeID);
            pstmt.setInt(4, shiftID);
            pstmt.setDate(5, Date.valueOf(LocalDate.now()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                return true;
            } else {
                return insertNewShift(employeeID, shiftID, startCash);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean insertNewShift(int employeeID, int shiftID, double startCash) {
        String sql = "INSERT INTO EmployeeShift (EmployeeID, ShiftID, WorkDate, CheckInTime, StartCash, TotalSales, EndCash) VALUES (?, ?, ?, ?, ?, 0, 0)";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeID);
            pstmt.setInt(2, shiftID);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));
            pstmt.setTime(4, Time.valueOf(LocalTime.now()));
            pstmt.setDouble(5, startCash);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkOut(int employeeID, int shiftID, double totalSales, double endCash) {
        String sql = "UPDATE EmployeeShift SET CheckOutTime = ?, TotalSales = ?, EndCash = ? "
                + "WHERE EmployeeID = ? AND ShiftID = ? AND WorkDate = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTime(1, Time.valueOf(LocalTime.now()));
            pstmt.setDouble(2, totalSales);
            pstmt.setDouble(3, endCash);
            pstmt.setInt(4, employeeID);
            pstmt.setInt(5, shiftID);
            pstmt.setDate(6, Date.valueOf(LocalDate.now()));

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
