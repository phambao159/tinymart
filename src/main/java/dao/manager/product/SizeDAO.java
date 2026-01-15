package dao.manager.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.manager.product.Size;
import util.DBConnection;

public class SizeDAO {

    private final DBConnection dc = new DBConnection();

    // 1. READ: Chỉ lấy danh sách các Size đang 'Active'
    public List<Size> getData() {
        List<Size> sizes = new ArrayList<>();
        String sql = "SELECT SizeID, Type, Status FROM Size WHERE Status = 'Active'";

        try (Connection conn = dc.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Size size = new Size(
                    rs.getInt("SizeID"),
                    rs.getString("Type"),
                    rs.getString("Status") // Giả định Model Size đã thêm field Status
                );
                sizes.add(size);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error when fetching sizes: " + e.getMessage());
            e.printStackTrace();
        }
        return sizes;
    }

    // 2. CREATE: Thêm mới mặc định Status là 'Active'
    public boolean insert(Size size) {
        String sql = "INSERT INTO Size (Type, Status) VALUES (?, 'Active')";
        try (Connection conn = dc.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, size.getType());
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("SQL Error when inserting size: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 3. UPDATE: Cập nhật thông tin Type
    public boolean update(Size size) {
        String sql = "UPDATE Size SET Type = ? WHERE SizeID = ?";
        try (Connection conn = dc.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, size.getType());
            ps.setInt(2, size.getSizeID());
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("SQL Error when updating size: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 4. DELETE (SOFT DELETE): Chuyển trạng thái sang 'Inactive'
    public boolean delete(int sizeID) {
        // Thay vì DELETE thực sự, ta UPDATE cột Status
        String sql = "UPDATE Size SET Status = 'Inactive' WHERE SizeID = ?";
        try (Connection conn = dc.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sizeID);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("SQL Error when soft deleting size: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 5. SEARCH: Tìm kiếm Type trong danh sách 'Active'
    public List<Size> searchByType(String keyword) {
        List<Size> sizes = new ArrayList<>();
        String sql = "SELECT SizeID, Type, Status FROM Size WHERE Type LIKE ? AND Status = 'Active'";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sizes.add(new Size(
                        rs.getInt("SizeID"),
                        rs.getString("Type"),
                        rs.getString("Status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error when searching size: " + e.getMessage());
            e.printStackTrace();
        }
        return sizes;
    }
}