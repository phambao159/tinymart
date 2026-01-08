package dao.Warehouse;

import model.Warehouse.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private final Connection conn;

    public NotificationDAO(Connection conn) {
        this.conn = conn;
    }

    // Lấy tất cả thông báo từ DB
    public List<Notification> getAllNotifications() throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.*, s.FullName AS SenderName, r.FullName AS ReceiverName "
                + "FROM Notification n "
                + "JOIN Employee s ON n.EmployeeID = s.EmployeeID "
                + "LEFT JOIN Employee r ON n.ReceiverID = r.EmployeeID";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Thêm mới thông báo
    public void insertNotification(Notification n) throws SQLException {
        String sql = "INSERT INTO Notification (EmployeeID, ReceiverID, Title, Content, SentDate, IsRead) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, n.getEmployeeID());
            if (n.getReceiverID() != null) {
                ps.setInt(2, n.getReceiverID());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, n.getTitle());
            ps.setString(4, n.getContent());
            ps.setTimestamp(5, Timestamp.valueOf(n.getSentDate()));
            ps.setBoolean(6, n.isRead());
            ps.executeUpdate();
        }
    }

    // Chuyển ResultSet thành đối tượng Notification
    private Notification mapRow(ResultSet rs) throws SQLException {
        Integer receiverId = null;
        Object recvObj = rs.getObject("ReceiverID");
        if (recvObj != null) {
            receiverId = rs.getInt("ReceiverID"); // tránh lỗi ClassCastException
        }

        return new Notification(
                rs.getLong("NotificationID"), // BIGINT → long
                rs.getInt("EmployeeID"), // INT → int
                receiverId, // INT → Integer (nullable)
                rs.getString("SenderName"), // alias từ JOIN
                rs.getString("ReceiverName"), // alias từ JOIN
                rs.getString("Title"),
                rs.getString("Content"),
                rs.getTimestamp("SentDate").toLocalDateTime(),// DATETIME → LocalDateTime
                rs.getBoolean("IsRead") // tinyint(1) → boolean
        );
    }

    public void updateNotificationStatus(int notificationId, boolean isRead) {
        String sql = "UPDATE Notifications SET isRead = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isRead);
            ps.setInt(2, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
