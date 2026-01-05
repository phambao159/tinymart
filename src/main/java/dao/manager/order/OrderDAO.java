package dao.manager.order;

import model.manager.order.Order;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Lấy danh sách tất cả hóa đơn
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `Order` ORDER BY OrderDateTime DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Thêm mới một hóa đơn
     */
    public boolean insert(Order order) {
        String sql = "INSERT INTO `Order` (OrderDateTime, EmployeeID, CustomerID, TotalAmount, DiscountAmount, PaymentMethod) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(order.getOrderDateTime()));
            ps.setInt(2, order.getEmployeeID());

            if (order.getCustomerID() != null) {
                ps.setInt(3, order.getCustomerID());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setDouble(4, order.getTotalAmount());
            ps.setDouble(5, order.getDiscountAmount());
            ps.setString(6, order.getPaymentMethod());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setOrderID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tìm kiếm hóa đơn theo ID hoặc tên khách hàng (cần JOIN)
     */
    public List<Order> searchOrders(String keyword) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.* FROM `Order` o "
                + "LEFT JOIN Customer c ON o.CustomerID = c.CustomerID "
                + "WHERE o.OrderID LIKE ? OR c.FullName LIKE ? "
                + "ORDER BY o.OrderDateTime DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Helper method để map dữ liệu từ ResultSet sang Object
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderID(rs.getInt("OrderID"));
        order.setOrderDateTime(rs.getTimestamp("OrderDateTime").toLocalDateTime());
        order.setEmployeeID(rs.getInt("EmployeeID"));

        int customerId = rs.getInt("CustomerID");
        order.setCustomerID(rs.wasNull() ? null : customerId);

        order.setTotalAmount(rs.getDouble("TotalAmount"));
        order.setDiscountAmount(rs.getDouble("DiscountAmount"));
        order.setPaymentMethod(rs.getString("PaymentMethod"));
        return order;
    }

    public List<Order> getRecentOrders(int limit) {
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT * FROM `Order` "
                + "WHERE TotalAmount > 0 "
                + "AND OrderDateTime <= NOW() "
                + "AND OrderDateTime >= DATE_SUB(NOW(), INTERVAL 30 DAY) "
                + "ORDER BY OrderDateTime DESC LIMIT ?";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách đơn hàng gần đây: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }
}
