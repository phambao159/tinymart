package dao.manager;

// Sử dụng file kết nối của bạn
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.manager.ProductSize;
import util.DBConnection;

public class ProductSizeDAO {


    public List<ProductSize> getByProductID(int productID) {
        List<ProductSize> list = new ArrayList<>();
        
        // SQL: Join ProductSize với Size để lấy thông tin hiển thị
        String sql = "SELECT ps.ProductSizeID, ps.ProductID, s.Type as SizeType, " +
                     "ps.CostPrice, ps.SellingPrice, ps.StockQuantity " +
                     "FROM ProductSize ps " +
                     "JOIN Size s ON ps.SizeID = s.SizeID " +
                     "WHERE ps.ProductID = ?";

        try  {
            DBConnection dc = new DBConnection();
            Connection con = dc.getConnect(); 
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, productID);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ProductSize ps = new ProductSize();
                ps.setSizeType(rs.getString("SizeType")); //
                ps.setCostPrice(rs.getDouble("CostPrice")); //
                ps.setSellingPrice(rs.getInt("SellingPrice")); //
                ps.setStockQuantity(rs.getInt("StockQuantity")); //
                
                list.add(ps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}