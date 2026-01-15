package dao.manager.product;

import model.manager.product.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;

public class CategoryDAO {

    DBConnection dc = new DBConnection();

    // 1. GET ALL: Chỉ lấy những Category đang hoạt động
    public List<Category> getData() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT CategoryID, Name, Description, Status FROM Category WHERE Status = 'Active'";
        
        try (Connection conn = dc.getConnect(); 
             PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("CategoryID"),
                        rs.getString("Name"),
                        rs.getString("Description"),
                        rs.getString("Status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error when fetching categories: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    // 2. INSERT: Mặc định Status trong Database nên là 'Active'
    public boolean insert(Category category) {
        String sql = "INSERT INTO Category (Name, Description, Status) VALUES (?, ?, 'Active')";
        try (Connection conn = dc.getConnect(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. UPDATE: Cập nhật thông tin
    public boolean update(Category category) {
        String sql = "UPDATE Category SET Name = ?, Description = ? WHERE CategoryID = ?";
        try (Connection conn = dc.getConnect(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getCategoryID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. DELETE (SOFT DELETE): Chuyển trạng thái thành Inactive
    public boolean delete(int categoryID) {
        String sql = "UPDATE Category SET Status = 'Inactive' WHERE CategoryID = ?";
        try (Connection conn = dc.getConnect(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. SEARCH: Chỉ tìm trong các Category 'Active'
    public List<Category> searchByName(String keyword) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT CategoryID, Name, Description, Status FROM Category WHERE Name LIKE ? AND Status = 'Active'";
        try (Connection conn = dc.getConnect(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                            rs.getInt("CategoryID"),
                            rs.getString("Name"),
                            rs.getString("Description"),
                            rs.getString("Status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // 6. GET BY ID
    public Category getByID(int categoryID) {
        String sql = "SELECT * FROM Category WHERE CategoryID = ? AND Status = 'Active'";
        try (Connection conn = dc.getConnect(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getInt("CategoryID"),
                            rs.getString("Name"),
                            rs.getString("Description"),
                            rs.getString("Status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}