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

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

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
                    } else {
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
        String sql = "SELECT CheckInTime FROM EmployeeShift WHERE EmployeeID = ? AND WorkDate = ? AND CheckInTime IS NOT NULL";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeID);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkIn(int employeeID, int requestedShiftID, double startCash) {
        Connection conn = null;
        try {
            conn = dc.getConnect();

            String findAssignedSql = "SELECT EmployeeShiftID, ShiftID FROM EmployeeShift "
                    + "WHERE EmployeeID = ? AND WorkDate = CURDATE() AND CheckInTime IS NULL";

            int assignedRowID = -1;

            try (PreparedStatement psFind = conn.prepareStatement(findAssignedSql)) {
                psFind.setInt(1, employeeID);
                ResultSet rs = psFind.executeQuery();
                if (rs.next()) {
                    assignedRowID = rs.getInt("EmployeeShiftID");
                }
            }

            if (assignedRowID != -1) {
                String updateSql = "UPDATE EmployeeShift SET CheckInTime = ?, StartCash = ? WHERE EmployeeShiftID = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setTime(1, Time.valueOf(LocalTime.now()));
                    psUpdate.setDouble(2, startCash);
                    psUpdate.setInt(3, assignedRowID);
                    return psUpdate.executeUpdate() > 0;
                }
            } else {
                return insertNewShift(employeeID, requestedShiftID, startCash);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private boolean insertNewShift(int employeeID, int shiftID, double startCash) {
        String sql = "INSERT INTO EmployeeShift (EmployeeID, ShiftID, WorkDate, CheckInTime, StartCash, TotalSales, EndCash) "
                + "VALUES (?, ?, CURDATE(), NOW(), ?, 0, 0)";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeID);
            pstmt.setInt(2, shiftID);
            pstmt.setDouble(3, startCash);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkOut(int employeeID, int shiftID, double totalSales, double endCash) {
        String sql = "UPDATE EmployeeShift SET CheckOutTime = ?, TotalSales = ?, EndCash = ? "
                + "WHERE EmployeeID = ? AND ShiftID = ? AND WorkDate = ? AND CheckOutTime IS NULL";

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

    public int getAssignedShiftID(int employeeId) {
        int shiftId = 0;
        String sql = "SELECT ShiftID FROM EmployeeShift WHERE EmployeeID = ? AND WorkDate = ? AND CheckInTime IS NULL LIMIT 1";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                shiftId = rs.getInt("ShiftID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shiftId;
    }
}
