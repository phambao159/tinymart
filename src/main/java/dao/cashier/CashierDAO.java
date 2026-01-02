package dao.cashier;

import model.cashier.Product;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.cashier.ProductSizeInfo;
import model.cashier.Category;
import model.cashier.Customer;
import java.time.LocalDate;

public class CashierDAO {

    DBConnection dc = new DBConnection();

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();

        String sql = "SELECT p.ProductID, p.Name, p.Image, p.CategoryID, "
                + "MIN(ps.SellingPrice) as Price, "
                + "SUM(ps.StockQuantity) as TotalStock "
                + "FROM product p "
                + "JOIN productsize ps ON p.ProductID = ps.ProductID "
                + "WHERE p.Status = 'Active' "
                + "GROUP BY p.ProductID, p.Name, p.Image, p.CategoryID";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int totalStock = rs.getInt("TotalStock");

                list.add(new Product(
                        String.valueOf(rs.getInt("ProductID")),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getString("Image"),
                        rs.getInt("CategoryID"),
                        totalStock
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.ProductID, p.Name, p.Image, p.CategoryID, "
                + "MIN(ps.SellingPrice) as Price, "
                + "SUM(ps.StockQuantity) as TotalStock "
                + "FROM product p "
                + "JOIN productsize ps ON p.ProductID = ps.ProductID "
                + "WHERE p.Status = 'Active' "
                + "AND (p.Name LIKE ? OR p.ProductID LIKE ?) "
                + "GROUP BY p.ProductID, p.Name, p.Image, p.CategoryID";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int totalStock = rs.getInt("TotalStock");

                list.add(new Product(
                        String.valueOf(rs.getInt("ProductID")),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getString("Image"),
                        rs.getInt("CategoryID"),
                        totalStock
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductSizeInfo> getProductSizes(int productId) {
        List<ProductSizeInfo> list = new ArrayList<>();
        String sql = "SELECT ps.SizeID, s.Type, ps.SellingPrice, ps.StockQuantity "
                + "FROM productsize ps "
                + "JOIN size s ON ps.SizeID = s.SizeID "
                + "WHERE ps.ProductID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new ProductSizeInfo(
                        rs.getInt("SizeID"),
                        rs.getString("Type"),
                        rs.getDouble("SellingPrice"),
                        rs.getInt("StockQuantity")
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
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("CategoryID"),
                        rs.getString("Name")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getEmployeeNameById(int id) {
        String name = "";
        String sql = "SELECT FullName FROM Employee WHERE EmployeeID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("FullName");
            }
        } catch (Exception e) {
        }
        return name;
    }
    
    public Customer findCustomerByPhone(String phone) {
        String sql = "SELECT * FROM Customer WHERE PhoneNumber = ?";
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                    rs.getInt("CustomerID"),
                    rs.getString("FullName"),
                    rs.getString("PhoneNumber"),
                    rs.getInt("Points"),
                    rs.getString("Email"),
                    rs.getDate("RegistrationDate")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean addCustomer(Customer c) {
        String sql = "INSERT INTO Customer (FullName, PhoneNumber, Email, Points, RegistrationDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getFullName());
            pstmt.setString(2, c.getPhoneNumber());
            pstmt.setString(3, c.getEmail());
            pstmt.setInt(4, 0);
            pstmt.setDate(5, Date.valueOf(LocalDate.now()));
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public void updateCustomerPoints(int customerId, int newTotalPoints) {
        String sql = "UPDATE Customer SET Points = ? WHERE CustomerID = ?";
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newTotalPoints);
            pstmt.setInt(2, customerId);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    public boolean reduceStock(int productId, int sizeId, int quantityToReduce) {
        String sql = "UPDATE ProductSize SET StockQuantity = StockQuantity - ? " +
                     "WHERE ProductID = ? AND SizeID = ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, quantityToReduce);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, sizeId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
