package dao.manager.report;

import model.manager.report.Report;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    private final DBConnection dc = new DBConnection();

    /**
     * HÀM CẬP NHẬT: Xử lý filter từ 2 ComboBox (Năm và Kỳ) Các filter có dạng:
     * "Year 2024", "2024 - Month 5", "2024 - Quarter 1"
     */
    public List<Report> getRevenueByFilter(String filter) {
        List<Report> list = new ArrayList<>();
        String sql = "";

        // TRƯỜNG HỢP 1: Chọn "All" -> Chỉ hiện các tháng đã qua hoặc đang diễn ra trong năm đó
        if (filter.startsWith("Year ")) {
            String year = filter.replace("Year ", "").trim();
            sql = "SELECT DATE_FORMAT(OrderDateTime, '%m/%Y') as DateLabel, SUM(TotalAmount) as Total "
                    + "FROM `Order` "
                    + "WHERE YEAR(OrderDateTime) = " + year + " AND OrderDateTime <= NOW() "
                    + "GROUP BY MONTH(OrderDateTime) ORDER BY MONTH(OrderDateTime) ASC";

        } // TRƯỜNG HỢP 2: Chọn "Month X" -> Chỉ hiện các ngày từ đầu tháng đến hiện tại
        else if (filter.contains(" - Month ")) {
            String[] parts = filter.split(" - Month ");
            sql = "SELECT DATE_FORMAT(OrderDateTime, '%d/%m') as DateLabel, SUM(TotalAmount) as Total "
                    + "FROM `Order` "
                    + "WHERE YEAR(OrderDateTime) = " + parts[0] + " AND MONTH(OrderDateTime) = " + parts[1]
                    + " AND OrderDateTime <= NOW() "
                    + "GROUP BY DATE(OrderDateTime) ORDER BY OrderDateTime ASC";

        } // TRƯỜNG HỢP 3: Chọn "Quarter X" -> Chỉ hiện các tháng trong quý nhưng không vượt quá hiện tại
        else if (filter.contains(" - Quarter ")) {
            String[] parts = filter.split(" - Quarter ");
            sql = "SELECT DATE_FORMAT(OrderDateTime, '%m/%Y') as DateLabel, SUM(TotalAmount) as Total "
                    + "FROM `Order` "
                    + "WHERE YEAR(OrderDateTime) = " + parts[0] + " AND QUARTER(OrderDateTime) = " + parts[1]
                    + " AND OrderDateTime <= NOW() "
                    + "GROUP BY MONTH(OrderDateTime) ORDER BY MONTH(OrderDateTime) ASC";
        }

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Report(rs.getString("DateLabel"), rs.getDouble("Total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * GIỮ NGUYÊN: Top 10 sản phẩm bán chạy
     */
    public List<Report> getTopSellingProducts(String filter) {
        List<Report> list = new ArrayList<>();
        String sql = "";
        String whereClause = "";

        // 1. Phân tách chuỗi filter để tạo điều kiện WHERE
        if (filter.startsWith("Year ")) {
            String year = filter.replace("Year ", "").trim();
            whereClause = "WHERE YEAR(o.OrderDateTime) = " + year;
        } else if (filter.contains(" - Month ")) {
            String[] parts = filter.split(" - Month ");
            whereClause = "WHERE YEAR(o.OrderDateTime) = " + parts[0] + " AND MONTH(o.OrderDateTime) = " + parts[1];
        } else if (filter.contains(" - Quarter ")) {
            String[] parts = filter.split(" - Quarter ");
            whereClause = "WHERE YEAR(o.OrderDateTime) = " + parts[0] + " AND QUARTER(o.OrderDateTime) = " + parts[1];
        }

        // Thêm điều kiện chặn tương lai
        whereClause += " AND o.OrderDateTime <= NOW() ";

        // 2. Câu lệnh SQL (Kết nối các bảng Order, OrderDetail và Product)
        sql = "SELECT p.Name, SUM(od.Quantity) AS TotalSold "
                + "FROM OrderDetail od "
                + "JOIN `Order` o ON od.OrderID = o.OrderID "
                + "JOIN ProductSize ps ON od.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + whereClause
                + " GROUP BY p.ProductID "
                + "ORDER BY TotalSold DESC LIMIT 10";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Report(rs.getString("Name"), rs.getDouble("TotalSold")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- CÁC HÀM THỐNG KÊ DASHBOARD (GIỮ NGUYÊN THEO CODE GỐC) ---
    public double getTotalRevenueLast30Days() {
        double total = 0;
        String sql = "SELECT SUM(TotalAmount) FROM `Order` WHERE OrderDateTime >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public int getOrdersToday() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM `Order` WHERE DATE(OrderDateTime) = CURDATE()";
        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public String getTopCategoryThisWeek() {
        String categoryName = "N/A";
        String sql = "SELECT c.Name FROM OrderDetail od JOIN `Order` o ON od.OrderID = o.OrderID "
                + "JOIN ProductSize ps ON od.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Category c ON p.CategoryID = c.CategoryID "
                + "WHERE YEARWEEK(o.OrderDateTime, 1) = YEARWEEK(CURDATE(), 1) "
                + "GROUP BY c.CategoryID, c.Name ORDER BY SUM(od.Quantity) DESC LIMIT 1";
        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                categoryName = rs.getString("Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryName;
    }

    public int getNewCustomersThisMonth() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM Customer WHERE MONTH(RegistrationDate) = MONTH(CURDATE()) AND YEAR(RegistrationDate) = YEAR(CURDATE())";
        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public double getRevenuePrevious30Days() {
        double total = 0;
        String sql = "SELECT SUM(TotalAmount) FROM `Order` WHERE OrderDateTime >= DATE_SUB(NOW(), INTERVAL 60 DAY) AND OrderDateTime < DATE_SUB(NOW(), INTERVAL 30 DAY)";
        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public int getOrdersYesterday() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM `Order` WHERE DATE(OrderDateTime) = SUBDATE(CURDATE(), 1)";
        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getNewCustomersLastMonth() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM Customer WHERE RegistrationDate >= DATE_SUB(DATE_FORMAT(NOW() ,'%Y-%m-01'), INTERVAL 1 MONTH) AND RegistrationDate < DATE_FORMAT(NOW() ,'%Y-%m-01')";
        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}
