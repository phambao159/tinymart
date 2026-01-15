package dao.cashier;
    
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import util.DBConnection;

public class EmployeeShiftDAO {

    DBConnection dc = new DBConnection();
    public int getCurrentShiftID() {
        LocalTime now = LocalTime.now();
        
        if (now.isBefore(LocalTime.of(14, 0))) {
            return 1;
        } else {
            return 2;
        }
    }

    public boolean checkIn(int employeeID, int shiftID, double startCash) {
        if (isCheckedIn(employeeID, shiftID)) return false;

        String sql = "INSERT INTO employeeshift (EmployeeID, ShiftID, WorkDate, CheckInTime, StartCash) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
        String sql = "UPDATE employeeshift SET CheckOutTime = ?, TotalSales = ?, EndCash = ? " +
                     "WHERE EmployeeID = ? AND ShiftID = ? AND WorkDate = ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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

    public boolean isCheckedIn(int employeeID, int shiftID) {
        String sql = "SELECT * FROM employeeshift WHERE EmployeeID = ? AND ShiftID = ? AND WorkDate = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeID);
            pstmt.setInt(2, shiftID);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}
