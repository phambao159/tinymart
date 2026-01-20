package dao.manager.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.manager.product.ProductSize;
import util.DBConnection;

public class ProductSizeDAO {

    private final DBConnection dc = new DBConnection();

    
    public int getStockQuantity(int productSizeID) {
        String sql = "SELECT COALESCE(SUM(id.Quantity + id.ShelfQuantity), 0) AS qty "
                   + "FROM ImportDetail id "
                   + "JOIN Import i ON id.ImportID = i.ImportID "
                   + "WHERE id.ProductSizeID = ? AND i.Status = 'Completed'";

        try (Connection con = dc.getConnect(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, productSizeID);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("qty");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in getStockQuantity: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Lấy danh sách Size theo ProductID (bao gồm PromotionID)
     */
    public List<ProductSize> getByProductID(int productID) {
        List<ProductSize> list = new ArrayList<>();
        // Bổ sung ps.PromotionID vào SELECT
        String sql = "SELECT ps.ProductSizeID, ps.ProductID, ps.SizeID, ps.PromotionID, "
                   + "s.Type AS SizeType, ps.CostPrice, ps.SellingPrice "
                   + "FROM ProductSize ps "
                   + "JOIN Size s ON ps.SizeID = s.SizeID "
                   + "WHERE ps.ProductID = ?";

        try (Connection con = dc.getConnect(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, productID);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ProductSize ps = new ProductSize();
                    ps.setProductSizeID(rs.getInt("ProductSizeID"));
                    ps.setProductID(rs.getInt("ProductID"));
                    ps.setSizeID(rs.getInt("SizeID"));
                    ps.setPromotionID(rs.getInt("PromotionID")); // Lấy PromotionID
                    ps.setSizeType(rs.getString("SizeType"));
                    ps.setCostPrice(rs.getDouble("CostPrice"));
                    ps.setSellingPrice(rs.getDouble("SellingPrice"));
                    
                    ps.setStockQuantity(getStockQuantity(ps.getProductSizeID()));
                    list.add(ps);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in getByProductID: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm mới một ProductSize kèm theo PromotionID
     */
    public boolean insert(ProductSize ps) {
        String sql = "INSERT INTO ProductSize (ProductID, SizeID, PromotionID, CostPrice, SellingPrice) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = dc.getConnect(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, ps.getProductID());
            pst.setInt(2, ps.getSizeID());
            
            // Xử lý nếu không có PromotionID (tránh lỗi khóa ngoại nếu truyền 0)
            if (ps.getPromotionID() > 0) {
                pst.setInt(3, ps.getPromotionID());
            } else {
                pst.setNull(3, Types.INTEGER);
            }
            
            pst.setDouble(4, ps.getCostPrice());
            pst.setDouble(5, ps.getSellingPrice());

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("❌ Error inserting ProductSize: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật ProductSize (bao gồm thay đổi khuyến mãi cho từng size)
     */
    public boolean update(ProductSize ps) {
        String sql = "UPDATE ProductSize "
                   + "SET SizeID = ?, PromotionID = ?, CostPrice = ?, SellingPrice = ? "
                   + "WHERE ProductSizeID = ?";

        try (Connection con = dc.getConnect(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, ps.getSizeID());
            
            if (ps.getPromotionID() > 0) {
                pst.setInt(2, ps.getPromotionID());
            } else {
                pst.setNull(2, Types.INTEGER);
            }
            
            pst.setDouble(3, ps.getCostPrice());
            pst.setDouble(4, ps.getSellingPrice());
            pst.setInt(5, ps.getProductSizeID());

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("❌ Error updating ProductSize: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa ProductSize
     */
    public boolean delete(int productSizeID) {
        String sql = "DELETE FROM ProductSize WHERE ProductSizeID = ?";
        try (Connection con = dc.getConnect(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, productSizeID);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("❌ Error deleting ProductSize: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách size cho TableView (Dùng chung logic để bảo trì dễ hơn)
     */
    public ArrayList<ProductSize> getSizesByProductId(int productId) {
        return new ArrayList<>(getByProductID(productId));
    }
}