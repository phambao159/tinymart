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

    // 1. READ: Lấy danh sách tất cả Size
    public List<Size> getData() {
        List<Size> sizes = new ArrayList<>();
        String sql = "SELECT SizeID, Type FROM Size";

        try (Connection conn = dc.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Size size = new Size(
                    rs.getInt("SizeID"),
                    rs.getString("Type")
                );
                sizes.add(size);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error when fetching sizes: " + e.getMessage());
            e.printStackTrace();
        }
        return sizes;
    }

    // 2. CREATE: Thêm mới Size
    public boolean insert(Size size) {
        String sql = "INSERT INTO Size (Type) VALUES (?)";
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

    // 3. UPDATE: Cập nhật Size
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

    // 4. DELETE: Xóa Size
    public boolean delete(int sizeID) {
        String sql = "DELETE FROM Size WHERE SizeID = ?";
        try (Connection conn = dc.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sizeID);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            // Lưu ý: Có thể lỗi nếu SizeID đang được sử dụng ở bảng ProductSize
            System.err.println("SQL Error when deleting size: " + e.getMessage());
            return false;
        }
    }

    // 5. SEARCH: Tìm kiếm theo loại Size (Type)
    public List<Size> searchByType(String keyword) {
        List<Size> sizes = new ArrayList<>();
        String sql = "SELECT SizeID, Type FROM Size WHERE Type LIKE ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sizes.add(new Size(
                        rs.getInt("SizeID"),
                        rs.getString("Type")
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