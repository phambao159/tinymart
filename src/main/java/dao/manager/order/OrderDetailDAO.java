package dao.manager.order;

import model.manager.order.OrderDetail;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Lấy danh sách chi tiết của một hóa đơn Lấy trực tiếp các trường giá từ
     * bảng OrderDetail
     */
    public List<OrderDetail> getDetailsByOrderId(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        // SQL lấy các trường giá từ chính bảng OrderDetail và JOIN để lấy tên hiển thị
        String sql = "SELECT od.*, p.Name AS ProductName, s.Type AS TypeName "
                + "FROM OrderDetail od "
                + "JOIN ProductSize ps ON od.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "WHERE od.OrderID = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrderDetailID(rs.getInt("OrderDetailID"));
                    detail.setOrderID(rs.getInt("OrderID"));
                    detail.setProductSizeID(rs.getInt("ProductSizeID"));
                    detail.setQuantity(rs.getInt("Quantity"));

                    // Lấy 3 trường giá mới từ DB
                    detail.setOriginalPrice(rs.getDouble("original_price"));
                    detail.setSellingPrice(rs.getDouble("selling_price"));
                    detail.setUnitCost(rs.getDouble("unit_cost"));

                    // Các trường thông tin thêm
                    detail.setProductName(rs.getString("ProductName"));
                    detail.setTypeName(rs.getString("TypeName"));

                    list.add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm mới chi tiết hóa đơn (Bao gồm cả các mức giá tại thời điểm bán)
     */
    public boolean insert(OrderDetail detail) {
        String sql = "INSERT INTO OrderDetail (OrderID, ProductSizeID, Quantity, original_price, selling_price, unit_cost) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, detail.getOrderID());
            ps.setInt(2, detail.getProductSizeID());
            ps.setInt(3, detail.getQuantity());
            ps.setDouble(4, detail.getOriginalPrice());
            ps.setDouble(5, detail.getSellingPrice());
            ps.setDouble(6, detail.getUnitCost());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thêm hàng loạt chi tiết hóa đơn (Dùng Transaction)
     */
    public boolean insertMultiple(List<OrderDetail> details) {
        String sql = "INSERT INTO OrderDetail (OrderID, ProductSizeID, Quantity, original_price, selling_price, unit_cost) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dc.getConnect();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (OrderDetail detail : details) {
                    ps.setInt(1, detail.getOrderID());
                    ps.setInt(2, detail.getProductSizeID());
                    ps.setInt(3, detail.getQuantity());
                    ps.setDouble(4, detail.getOriginalPrice());
                    ps.setDouble(5, detail.getSellingPrice());
                    ps.setDouble(6, detail.getUnitCost());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
