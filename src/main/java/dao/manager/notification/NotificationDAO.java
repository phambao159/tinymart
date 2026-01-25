package dao.manager.notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.manager.notification.Notification;
import util.DBConnection;

public class NotificationDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * HÀM MAPPING DÙNG CHUNG Giúp code sạch hơn và dễ bảo trì khi cấu trúc bảng
     * thay đổi
     */
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

    // 1. Lấy 10 thông báo đến mới nhất
    public List<Notification> getData() throws Exception {
        List<Notification> list = new ArrayList<>();
        // LIMIT 10 trực tiếp trong SQL
        String sql = "SELECT * FROM Notification WHERE ReceiverID = 1 "
                + "AND SentDate <= NOW() ORDER BY SentDate DESC LIMIT 10";

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

    // 3. Thêm thông báo mới
    public void insert(Notification n) throws Exception {
        String sql = "INSERT INTO Notification (EmployeeID, ReceiverID, Title, Content, SentDate, IsRead) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
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

    // 4. Cập nhật (thường dùng để đánh dấu đã đọc)
    public void update(Notification n) throws Exception {
        String sql = "UPDATE Notification SET EmployeeID = ?, ReceiverID = ?, Title = ?, "
                + "Content = ?, SentDate = ?, IsRead = ? WHERE NotificationID = ?";
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

    // 5. Đếm số lượng (Dùng cho Badge thông báo)
    public int countNoti(String type) throws Exception {
        boolean unreadOnly = "Unread".equalsIgnoreCase(type);
        String sql = "SELECT COUNT(*) FROM Notification WHERE ReceiverID = 1"
                + (unreadOnly ? " AND IsRead = 0" : "");

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // 6. Tìm kiếm 10 kết quả phù hợp nhất
    public List<Notification> searchNotification(String keyword) throws Exception {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE (Title LIKE ? OR Content LIKE ?) "
                + "AND ReceiverID = 1 ORDER BY SentDate DESC";

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

    // 7. Lấy 10 thông báo đã gửi mới nhất
    public List<Notification> getSentNoti() throws Exception {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE EmployeeID = 1 "
                + "AND SentDate <= NOW() ORDER BY SentDate DESC LIMIT 10";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNotification(rs));
            }
        }
        return list;
    }

    // 8. Xóa thông báo
    public void delete(long id) throws Exception {
        String sql = "DELETE FROM Notification WHERE NotificationID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }
    // 9. Tìm kiếm trong danh sách thông báo ĐÃ GỬI (chỉ lấy 10 kết quả)

    public List<Notification> searchsentNotification(String keyword) throws Exception {
        List<Notification> list = new ArrayList<>();
        // Điều kiện: EmployeeID = 1 (Người gửi) và khớp từ khóa Title/Content
        String sql = "SELECT * FROM Notification WHERE EmployeeID = 1 "
                + "AND (Title LIKE ? OR Content LIKE ?) "
                + "ORDER BY SentDate DESC ";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi khi tìm kiếm thông báo đã gửi: " + e.getMessage(), e);
        }
        return list;
    }
}
