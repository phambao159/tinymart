package dao.Warehouse;

import util.DBConnection;
import model.manager.employee.Employee;

import java.sql.*;
import java.time.LocalDate;

public class EmployeeShiftDAO {

    // Check-in: chỉ ghi lần đầu trong ngày
    public void checkIn(Employee employee, LocalDate workDate) throws SQLException {
        Connection conn = new DBConnection().getConnect();

        String updateSql = "UPDATE EmployeeShift SET CheckInTime = CURTIME() "
                + "WHERE EmployeeID = ? AND WorkDate = ? AND CheckInTime IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, employee.getEmployeeID());
            stmt.setDate(2, java.sql.Date.valueOf(workDate));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ [DEBUG] Check-in đầu tiên thành công cho EmployeeID="
                        + employee.getEmployeeID() + " | WorkDate=" + workDate);
            } else {
                System.out.println("⚠️ [DEBUG] Đã có Check-in trước đó cho EmployeeID="
                        + employee.getEmployeeID() + " | WorkDate=" + workDate);
            }
        }
    }

// Check-out: luôn ghi lần cuối trong ngày
    public void checkOut(Employee employee, LocalDate workDate) throws SQLException {
        Connection conn = new DBConnection().getConnect();

        String updateSql = "UPDATE EmployeeShift SET CheckOutTime = CURTIME() "
                + "WHERE EmployeeID = ? AND WorkDate = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, employee.getEmployeeID());
            stmt.setDate(2, java.sql.Date.valueOf(workDate));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ [DEBUG] Check-out (lần cuối) thành công cho EmployeeID="
                        + employee.getEmployeeID() + " | WorkDate=" + workDate);
            } else {
                System.out.println("⚠️ [DEBUG] Không tìm thấy bản ghi EmployeeShift để check-out cho EmployeeID="
                        + employee.getEmployeeID() + " | WorkDate=" + workDate);
            }
        }
    }

    // Giữ nguyên: Xác định ca hiện tại dựa vào giờ hệ thống
    private int getShiftIdByCurrentTime(Connection conn) throws SQLException {
        String sql = "SELECT ShiftID FROM Shift WHERE CURTIME() BETWEEN StartTime AND EndTime LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int shiftId = rs.getInt("ShiftID");
                System.out.println("🔵 [DEBUG] Xác định ShiftID hiện tại = " + shiftId);
                return shiftId;
            }
        }
        System.out.println("⚠️ [DEBUG] Không tìm thấy ShiftID phù hợp với giờ hiện tại. Mặc định ShiftID=1");
        return 1; // mặc định Morning nếu không tìm thấy
    }
}
