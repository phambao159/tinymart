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

    public List<Category> getData() {

        List<Category> categories = new ArrayList<>();

        String sql = "SELECT CategoryID, Name, Description FROM Category";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Category category = new Category(
                        rs.getInt("CategoryID"),
                        rs.getString("Name"),
                        rs.getString("Description")
                );
                categories.add(category);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error when fetching categories: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    public boolean insert(Category category) {
        String sql = "INSERT INTO Category (Name, Description) VALUES (?, ?)";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. UPDATE: Cập nhật thông tin category
    public boolean update(Category category) {
        String sql = "UPDATE Category SET Name = ?, Description = ? WHERE CategoryID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getCategoryID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. DELETE: Xóa category theo ID
    public boolean delete(int categoryID) {
        String sql = "DELETE FROM Category WHERE CategoryID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. SEARCH: Tìm kiếm category theo tên (gần đúng)
    public List<Category> searchByName(String keyword) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT CategoryID, Name, Description FROM Category WHERE Name LIKE ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                            rs.getInt("CategoryID"),
                            rs.getString("Name"),
                            rs.getString("Description")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // 6. GET BY ID: Lấy một category cụ thể (hữu ích cho việc đổ dữ liệu vào form edit)
    public Category getByID(int categoryID) {
        String sql = "SELECT * FROM Category WHERE CategoryID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getInt("CategoryID"),
                            rs.getString("Name"),
                            rs.getString("Description")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
