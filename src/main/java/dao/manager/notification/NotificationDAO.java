package dao.manager.notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.manager.notification.Notification;
import util.DBConnection;

public class NotificationDAO {

    private final DBConnection dc = new DBConnection();

    // HÀM DÙNG CHUNG: Mapping dữ liệu để tránh lặp code (Don't Repeat Yourself)
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationID(rs.getLong("NotificationID"));
        n.setEmployeeID(rs.getInt("EmployeeID"));
        n.setReceiverID(rs.getInt("ReceiverID"));
        n.setTitle(rs.getString("Title"));
        n.setContent(rs.getString("Content"));
        n.setSentDate(rs.getTimestamp("SentDate"));
        n.setIsRead(rs.getBoolean("IsRead"));
        return n;
    }

    // 1. Lấy tất cả thông báo (Đã gộp mapping)
    public List<Notification> getData() throws Exception {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE ReceiverID = 1 AND SentDate <= NOW() ORDER BY SentDate DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi truy vấn danh sách thông báo.", e);
        }
        return list;
    }

    // 2. Lấy thông báo theo ID
    public Notification getNotibyID(long id) throws Exception {
        String sql = "SELECT * FROM Notification WHERE NotificationID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {

                    return mapResultSetToNotification(rs);
                }
            }
        }
        return null;
    }

    // 3. Thêm thông báo
    public void insert(Notification n) throws Exception {
        String sql = "INSERT INTO Notification (EmployeeID, ReceiverID, Title, Content, SentDate, IsRead) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, n.getEmployeeID());
            pstmt.setInt(2, n.getReceiverID());
            pstmt.setString(3, n.getTitle());
            pstmt.setString(4, n.getContent());
            pstmt.setTimestamp(5, new java.sql.Timestamp(n.getSentDate().getTime()));
            pstmt.setBoolean(6, n.isIsRead());
            pstmt.executeUpdate();

        }
    }

    // 4. Cập nhật thông báo
    public void update(Notification n) throws Exception {
        String sql = "UPDATE Notification SET EmployeeID = ?, ReceiverID = ?, Title = ?, Content = ?, SentDate = ?, IsRead = ? WHERE NotificationID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, n.getEmployeeID());
            pstmt.setInt(2, n.getReceiverID());
            pstmt.setString(3, n.getTitle());
            pstmt.setString(4, n.getContent());
            pstmt.setTimestamp(5, new java.sql.Timestamp(n.getSentDate().getTime()));
            pstmt.setBoolean(6, n.isIsRead());
            pstmt.setLong(7, n.getNotificationID());
            pstmt.executeUpdate();

        }
    }

    // 5. Đếm số lượng thông báo (Tối ưu SQL)
    public int countNoti(String type) throws Exception {
        boolean unreadOnly = "Unread".equalsIgnoreCase(type);
        String sql = "SELECT COUNT(*) FROM Notification WHERE ReceiverID = 1" + (unreadOnly ? " AND IsRead = 0" : "");

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // 6. Tìm kiếm thông báo (Sử dụng hàm map dùng chung)
    public List<Notification> searchNotification(String keyword) throws Exception {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE (Title LIKE ? OR Content LIKE ?) ORDER BY SentDate DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        }
        return list;
    }

    public List<Notification> searchsentNotification(String keyword) throws Exception {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE (Title LIKE ? OR Content LIKE ?) AND EmployeeID = 1 ORDER BY SentDate DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        }
        return list;
    }

    // 7. Xóa thông báo
    public void delete(long id) throws Exception {
        String sql = "DELETE FROM Notification WHERE NotificationID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }
    // Lấy danh sách thông báo đã gửi (Mặc định Manager có ID = 1)

    public List<Notification> getSentNoti() throws Exception {
        List<Notification> list = new ArrayList<>();
        // Câu lệnh lấy các thông báo do EmployeeID = 1 gửi đi, sắp xếp mới nhất lên đầu
        String sql = "SELECT * FROM Notification WHERE EmployeeID = 1 AND SentDate <= NOW() ORDER BY SentDate DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Sử dụng lại hàm mapResultSetToNotification đã viết ở bước trước để tối ưu
                list.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi khi lấy danh sách thông báo đã gửi: " + e.getMessage(), e);
        }
        return list;
    }
}
