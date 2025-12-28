package dao.cashier;

import model.cashier.Product;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.cashier.ProductSizeInfo;
import model.cashier.Category;

public class CashierDAO {
    
    DBConnection dc = new DBConnection();

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();

        String sql = "SELECT p.ProductID, p.Name, p.Image, p.CategoryID, MIN(ps.SellingPrice) as Price " +
                     "FROM product p " +
                     "JOIN productsize ps ON p.ProductID = ps.ProductID " +
                     "WHERE p.Status = 'Active' " +
                     "GROUP BY p.ProductID, p.Name, p.Image, p.CategoryID"; // Group by cả CategoryID

        try (Connection conn = dc.getConnect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("ProductID"));
                String name = rs.getString("Name");
                double price = rs.getDouble("Price");
                String img = rs.getString("Image");
                int catId = rs.getInt("CategoryID");
                list.add(new Product(id, name, price, img, catId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.ProductID, p.Name, p.Image, p.CategoryID, MIN(ps.SellingPrice) as Price " +
                     "FROM product p " +
                     "JOIN productsize ps ON p.ProductID = ps.ProductID " +
                     "WHERE p.Status = 'Active' AND (p.Name LIKE ? OR p.ProductID LIKE ?) " +
                     "GROUP BY p.ProductID, p.Name, p.Image, p.CategoryID"; // Group by cả CategoryID
        
        try (Connection conn = dc.getConnect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Product(
                    String.valueOf(rs.getInt("ProductID")),
                    rs.getString("Name"),
                    rs.getDouble("Price"),
                    rs.getString("Image"),
                    rs.getInt("CategoryID")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductSizeInfo> getProductSizes(int productId) {
        List<ProductSizeInfo> list = new ArrayList<>();
        String sql = "SELECT ps.SizeID, s.Type, ps.SellingPrice " +
                     "FROM productsize ps " +
                     "JOIN size s ON ps.SizeID = s.SizeID " +
                     "WHERE ps.ProductID = ?";
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new ProductSizeInfo(
                    rs.getInt("SizeID"),
                    rs.getString("Type"),
                    rs.getDouble("SellingPrice")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT CategoryID, Name FROM Category";
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("CategoryID"),
                    rs.getString("Name")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    public String getEmployeeNameById(int id) {
    String name = "";
    String sql = "SELECT FullName FROM Employee WHERE EmployeeID = ?";
    try (Connection conn = dc.getConnect(); 
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            name = rs.getString("FullName");
        }
    } catch (Exception e) {}
    return name;
}
}