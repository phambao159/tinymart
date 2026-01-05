package dao.manager.notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.manager.notification.Notification;
import util.DBConnection;

public class NotificationDAO {

    private final DBConnection dc = new DBConnection();

    // 1. Lấy tất cả thông báo
    public List<Notification> getData() throws Exception {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE ReceiverID = 1 ORDER BY SentDate DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationID(rs.getLong("NotificationID"));
                    n.setEmployeeID(rs.getInt("EmployeeID"));
                    n.setReceiverID(rs.getInt("ReceiverID"));
                    n.setTitle(rs.getString("Title"));
                    n.setContent(rs.getString("Content"));
                    n.setSentDate(rs.getTimestamp("SentDate"));
                    n.setIsRead(rs.getBoolean("IsRead"));
                    list.add(n);
                }
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
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi truy vấn thông báo theo ID.", e);
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
        } catch (SQLException e) {
            throw new Exception("Lỗi khi thêm thông báo mới.", e);
        }
    }

    // 4. Xóa thông báo
    public void delete(long id) throws Exception {
        String sql = "DELETE FROM Notification WHERE NotificationID = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Lỗi khi xóa thông báo.", e);
        }
    }

    // 5. Cập nhật thông báo
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
        } catch (SQLException e) {
            throw new Exception("Lỗi khi cập nhật thông báo.", e);
        }
    }

    public int countNoti(String type) throws Exception {
        String sql = "";

        // 1. Xác định câu lệnh SQL dựa trên loại (type)
        if (type.equalsIgnoreCase("All")) {
            sql = "SELECT COUNT(*) FROM Notification WHERE ReceiverID = 1";
        } else if (type.equalsIgnoreCase("Unread")) {
            sql = "SELECT COUNT(*) FROM Notification WHERE ReceiverID = 1 AND IsRead = 0";
        } else {
            return 0; // Hoặc ném lỗi nếu type không hợp lệ
        }

        // 2. Thực hiện truy vấn
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1); // Lấy giá trị ở cột đầu tiên của kết quả COUNT
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi khi đếm số lượng thông báo: " + e.getMessage(), e);
        }

        return 0;
    }
    public List<Notification> getSentNoti() throws Exception {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE EmployeeID = 1 ORDER BY SentDate DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationID(rs.getLong("NotificationID"));
                    n.setEmployeeID(rs.getInt("EmployeeID"));
                    n.setReceiverID(rs.getInt("ReceiverID"));
                    n.setTitle(rs.getString("Title"));
                    n.setContent(rs.getString("Content"));
                    n.setSentDate(rs.getTimestamp("SentDate"));
                    n.setIsRead(rs.getBoolean("IsRead"));
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi truy vấn danh sách thông báo.", e);
        }
        return list;
    }
    // 6. Tìm kiếm thông báo theo Title hoặc Content
    public List<Notification> searchNotification(String keyword) throws Exception {
        List<Notification> list = new ArrayList<>();
        // Sử dụng LIKE để tìm kiếm chuỗi chứa từ khóa, không phân biệt hoa thường (tùy cấu hình DB)
        String sql = "SELECT * FROM Notification WHERE Title LIKE ? OR Content LIKE ? ORDER BY SentDate DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Thiết lập tham số với dấu % để tìm kiếm kiểu "chứa cụm từ"
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationID(rs.getLong("NotificationID"));
                    n.setEmployeeID(rs.getInt("EmployeeID"));
                    n.setReceiverID(rs.getInt("ReceiverID"));
                    n.setTitle(rs.getString("Title"));
                    n.setContent(rs.getString("Content"));
                    n.setSentDate(rs.getTimestamp("SentDate"));
                    n.setIsRead(rs.getBoolean("IsRead"));
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi khi tìm kiếm thông báo.", e);
        }
        return list;
    }
}
