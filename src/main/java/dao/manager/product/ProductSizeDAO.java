package dao.manager.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.manager.product.ProductSize;
import util.DBConnection;

public class ProductSizeDAO {

    /**
     * Lấy danh sách size của một sản phẩm để hiển thị lên TableView Đã cập nhật
     * để lấy đầy đủ các ID
     */
    public List<ProductSize> getByProductID(int productID) {
        List<ProductSize> list = new ArrayList<>();
        // SQL: Lấy đầy đủ ProductSizeID để phục vụ xóa/sửa sau này
        String sql = "SELECT ps.ProductSizeID, ps.ProductID, ps.SizeID, s.Type as SizeType, "
                + "ps.CostPrice, ps.SellingPrice, ps.StockQuantity "
                + "FROM ProductSize ps "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "WHERE ps.ProductID = ?";

        try (Connection con = new DBConnection().getConnect(); PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, productID);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ProductSize ps = new ProductSize();
                    // Set đầy đủ thông tin từ DB vào Model
                    ps.setProductSizeID(rs.getInt("ProductSizeID"));
                    ps.setProductID(rs.getInt("ProductID"));
                    ps.setSizeID(rs.getInt("SizeID"));
                    ps.setSizeType(rs.getString("SizeType"));
                    ps.setCostPrice(rs.getDouble("CostPrice"));
                    ps.setSellingPrice(rs.getInt("SellingPrice"));
                    ps.setStockQuantity(rs.getInt("StockQuantity"));

                    list.add(ps);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi getByProductID: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm một dòng mới vào bảng ProductSize Sử dụng cho chức năng thêm size
     * ngay tại màn hình Detail
     */
    public boolean insert(ProductSize ps) {
        // Cấu trúc INSERT khớp với file SQL bạn gửi
        String sql = "INSERT INTO ProductSize (ProductID, SizeID, CostPrice, SellingPrice, StockQuantity) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = new DBConnection().getConnect(); PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, ps.getProductID());
            pst.setInt(2, ps.getSizeID());
            pst.setDouble(3, ps.getCostPrice());
            pst.setDouble(4, ps.getSellingPrice());
            pst.setInt(5, ps.getStockQuantity());

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            // Lỗi thường gặp: Thêm trùng ProductID + SizeID nếu DB có ràng buộc Unique
            System.err.println("Lỗi insert ProductSize: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gợi ý thêm: Hàm xóa size nếu bạn muốn tích hợp vào TableView sau này
     */
    // Thêm vào ProductSizeDAO.java
    public boolean update(ProductSize ps) {
        String sql = "UPDATE ProductSize SET SizeID = ?, CostPrice = ?, SellingPrice = ?, StockQuantity = ? "
                + "WHERE ProductSizeID = ?";
        try (Connection con = new DBConnection().getConnect(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, ps.getSizeID());
            pst.setDouble(2, ps.getCostPrice());
            pst.setDouble(3, ps.getSellingPrice());
            pst.setInt(4, ps.getStockQuantity());
            pst.setInt(5, ps.getProductSizeID());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int productSizeID) {
        String sql = "DELETE FROM ProductSize WHERE ProductSizeID = ?";
        try (Connection con = new DBConnection().getConnect(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, productSizeID);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<ProductSize> getSizesByProductId(int productId) {
        ArrayList<ProductSize> list = new ArrayList<>();
        // Use s.Type to fill sizeType in your model
        String sql = "SELECT ps.*, s.Type "
                + "FROM ProductSize ps "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "WHERE ps.ProductID = ?";

        try (Connection con = new DBConnection().getConnect(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductSize size = new ProductSize();
                    size.setProductSizeID(rs.getInt("ProductSizeID"));
                    size.setProductID(rs.getInt("ProductID"));
                    size.setSizeID(rs.getInt("SizeID"));
                    size.setStockQuantity(rs.getInt("StockQuantity"));
                    size.setCostPrice(rs.getDouble("CostPrice"));
                    size.setSellingPrice(rs.getDouble("SellingPrice"));

                    size.setSizeType(rs.getString("Type"));

                    list.add(size);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ProductSizes: " + e.getMessage());
        }
        return list;
    }

}
