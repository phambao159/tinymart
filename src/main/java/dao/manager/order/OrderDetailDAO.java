package dao.manager.order;

import model.manager.order.OrderDetail;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * Lấy danh sách chi tiết của một hóa đơn để hiển thị tên sản phẩm Đã bỏ
     * SalePrice, chỉ lấy ProductName và TypeName
     */
    public List<OrderDetail> getDetailsByOrderId(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        // Thêm ps.SellingPrice vào câu lệnh SELECT
        String sql = "SELECT od.*, p.Name AS ProductName, s.Type AS TypeName, ps.SellingPrice "
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

                    // Lấy SellingPrice từ bảng ProductSize và gán vào field UnitPrice của Model
                    detail.setUnitPrice(rs.getDouble("SellingPrice"));

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
     * Thêm mới chi tiết hóa đơn (Chỉ gồm ID và số lượng)
     */
    public boolean insert(OrderDetail detail) {
        String sql = "INSERT INTO OrderDetail (OrderID, ProductSizeID, Quantity) VALUES (?, ?, ?)";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, detail.getOrderID());
            ps.setInt(2, detail.getProductSizeID());
            ps.setInt(3, detail.getQuantity());

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
        String sql = "INSERT INTO OrderDetail (OrderID, ProductSizeID, Quantity) VALUES (?, ?, ?)";
        Connection conn = null;
        try {
            conn = dc.getConnect();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (OrderDetail detail : details) {
                    ps.setInt(1, detail.getOrderID());
                    ps.setInt(2, detail.getProductSizeID());
                    ps.setInt(3, detail.getQuantity());
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
