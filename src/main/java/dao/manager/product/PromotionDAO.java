package dao.manager.product;

import model.manager.product.Promotion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;

public class PromotionDAO {
    private final DBConnection dc = new DBConnection();

    public List<Promotion> getData() {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM Promotion";
        try (Connection conn = dc.getConnect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPromotion(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(Promotion p) {
        String sql = "INSERT INTO Promotion (Name, Description, Type, Value, StartDate, EndDate, Status) VALUES (?,?,?,?,?,?,?)";
        return executeUpdate(sql, p, false);
    }

    public boolean update(Promotion p) {
        String sql = "UPDATE Promotion SET Name=?, Description=?, Type=?, Value=?, StartDate=?, EndDate=?, Status=? WHERE PromotionID=?";
        return executeUpdate(sql, p, true);
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM Promotion WHERE PromotionID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Promotion> searchByName(String keyword) {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM Promotion WHERE Name LIKE ?";
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToPromotion(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private boolean executeUpdate(String sql, Promotion p, boolean isUpdate) {
        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getType());
            ps.setDouble(4, p.getValue());
            ps.setDate(5, p.getStartDate() != null ? Date.valueOf(p.getStartDate()) : null);
            ps.setDate(6, p.getEndDate() != null ? Date.valueOf(p.getEndDate()) : null);
            ps.setString(7, p.getStatus());
            if (isUpdate) ps.setInt(8, p.getPromotionID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Promotion mapResultSetToPromotion(ResultSet rs) throws SQLException {
        return new Promotion(
            rs.getInt("PromotionID"), rs.getString("Name"), rs.getString("Description"),
            rs.getString("Type"), rs.getDouble("Value"),
            rs.getDate("StartDate") != null ? rs.getDate("StartDate").toLocalDate() : null,
            rs.getDate("EndDate") != null ? rs.getDate("EndDate").toLocalDate() : null,
            rs.getString("Status")
        );
    }
}