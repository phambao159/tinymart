package dao.manager.order;

import model.manager.order.Order;
import util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Lấy danh sách tất cả hóa đơn
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        // JOIN để lấy FullName của Nhân viên và Khách hàng
        String sql = "SELECT o.*, c.FullName as CustomerName, e.FullName as EmployeeName "
            + "FROM `Order` o "
            + "LEFT JOIN Customer c ON o.CustomerID = c.CustomerID "
            + "JOIN Employee e ON o.EmployeeID = e.EmployeeID "
            + "WHERE o.OrderDateTime <= NOW() " // WHERE phải đứng trước ORDER BY
            + "ORDER BY o.OrderDateTime DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Gán thêm tên vào object
                order.setCustomerName(rs.getString("CustomerName") != null ? rs.getString("CustomerName") : "Guest");
                order.setEmployeeName(rs.getString("EmployeeName"));
                orders.add(order);
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
    public List<Order> searchOrdersAdvanced(String keyword, Integer employeeID, LocalDate fromDate, LocalDate toDate) {
        List<Order> orders = new ArrayList<>();

        // Base SQL với JOIN để lấy tên thay vì ID
        StringBuilder sql = new StringBuilder(
                "SELECT o.*, c.FullName as CustomerName, e.FullName as EmployeeName "
                + "FROM `Order` o "
                + "LEFT JOIN Customer c ON o.CustomerID = c.CustomerID "
                + "JOIN Employee e ON o.EmployeeID = e.EmployeeID "
                + "WHERE 1=1 "
        );

        // Cộng dồn điều kiện filter
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (o.OrderID LIKE ? OR c.FullName LIKE ?) ");
        }
        if (employeeID != null) {
            sql.append("AND o.EmployeeID = ? ");
        }
        if (fromDate != null) {
            sql.append("AND DATE(o.OrderDateTime) >= ? ");
        }
        if (toDate != null) {
            sql.append("AND DATE(o.OrderDateTime) <= ? ");
        }

        sql.append("ORDER BY o.OrderDateTime DESC");

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
                ps.setString(index++, "%" + keyword + "%");
            }
            if (employeeID != null) {
                ps.setInt(index++, employeeID);
            }
            if (fromDate != null) {
                ps.setDate(index++, Date.valueOf(fromDate));
            }
            if (toDate != null) {
                ps.setDate(index++, Date.valueOf(toDate));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    // Gán thêm thông tin tên từ kết quả JOIN
                    order.setCustomerName(rs.getString("CustomerName") != null ? rs.getString("CustomerName") : "Guest");
                    order.setEmployeeName(rs.getString("EmployeeName"));
                    orders.add(order);
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
