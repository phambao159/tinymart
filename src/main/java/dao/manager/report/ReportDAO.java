package dao.manager.report;

import model.manager.report.Report;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    private final DBConnection dc = new DBConnection();

    // 1. Thống kê Doanh thu theo ngày (Dùng cho LineChart)
    public List<Report> getRevenueByFilter(String filter) {
        List<Report> list = new ArrayList<>();
        String sql = "";

        switch (filter) {
            case "Last 7 days":
                
                sql = "SELECT DATE(OrderDateTime) as DateLabel, SUM(TotalAmount) as Total "
                        + "FROM `Order` "
                        + "WHERE DATE(OrderDateTime) BETWEEN DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND CURDATE() "
                        + "GROUP BY DATE(OrderDateTime) "
                        + "ORDER BY DateLabel ASC";
                break;

            case "Last Month":
                
                sql = "SELECT DATE(OrderDateTime) as DateLabel, SUM(TotalAmount) as Total "
                        + "FROM `Order` "
                        + "WHERE DATE(OrderDateTime) BETWEEN DATE_SUB(CURDATE(), INTERVAL 1 MONTH) AND CURDATE() "
                        + "GROUP BY DATE(OrderDateTime) "
                        + "ORDER BY DateLabel ASC";
                break;

            case "Last Year":
                
                sql = "SELECT DATE_FORMAT(OrderDateTime, '%Y-%m') as DateLabel, SUM(TotalAmount) as Total "
                        + "FROM `Order` "
                        + "WHERE OrderDateTime BETWEEN DATE_SUB(CURDATE(), INTERVAL 1 YEAR) AND NOW() "
                        + "GROUP BY DateLabel "
                        + "ORDER BY DateLabel ASC";
                break;

            default:
                if (filter.startsWith("Month")) {
                    String monthNum = filter.replace("Month ", "").trim();
                    
                    sql = "SELECT DATE(OrderDateTime) as DateLabel, SUM(TotalAmount) as Total "
                            + "FROM `Order` "
                            + "WHERE MONTH(OrderDateTime) = " + monthNum + " "
                            + "AND YEAR(OrderDateTime) = YEAR(CURDATE()) "
                            + "AND OrderDateTime <= NOW() " 
                            + "GROUP BY DATE(OrderDateTime) "
                            + "ORDER BY DateLabel ASC";
                } else {
         
                    sql = "SELECT DATE(OrderDateTime) as DateLabel, SUM(TotalAmount) as Total "
                            + "FROM `Order` "
                            + "WHERE DATE(OrderDateTime) <= CURDATE() "
                            + "GROUP BY DATE(OrderDateTime) "
                            + "ORDER BY DateLabel DESC LIMIT 15";
                }
                break;
        }

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // "DateLabel" sẽ trả về chuỗi ngày hoặc tháng tùy theo câu SQL trên
                list.add(new Report(rs.getString("DateLabel"), rs.getDouble("Total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Report> getTopSellingProducts() {
        List<Report> list = new ArrayList<>();
        // SQL: Kết hợp Product Name và Size Name, Group theo ProductSizeID
        String sql = "SELECT CONCAT(p.Name, ' (', s.Type, ')') AS FullName, SUM(od.Quantity) AS TotalSold "
                + "FROM OrderDetail od "
                + "JOIN ProductSize ps ON od.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "GROUP BY od.ProductSizeID "
                + "ORDER BY TotalSold DESC LIMIT 10";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Lấy cột FullName (ví dụ: "Coca Cola (M)") và TotalSold
                list.add(new Report(rs.getString("FullName"), rs.getDouble("TotalSold")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 1. Tổng doanh thu 30 ngày qua
    public double getTotalRevenueLast30Days() {
        double total = 0;
        // Tên cột đúng là OrderDateTime thay vì OrderDate
        String sql = "SELECT SUM(TotalAmount) FROM `Order` "
                + "WHERE OrderDateTime >= DATE_SUB(NOW(), INTERVAL 30 DAY)";

        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    // 2. Số đơn hàng hôm nay
    public int getOrdersToday() {
        int count = 0;
        // Sử dụng hàm DATE() để so sánh với OrderDateTime
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

    // 3. Danh mục bán chạy nhất (Dựa trên số lượng trong OrderDetail)
    public String getTopCategoryThisWeek() {
        String categoryName = "N/A";
        // SQL: Join thêm bảng Order để lọc theo tuần hiện tại (YEARWEEK)
        String sql = "SELECT c.Name "
                + "FROM OrderDetail od "
                + "JOIN `Order` o ON od.OrderID = o.OrderID " // Thêm join với bảng Order
                + "JOIN ProductSize ps ON od.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Category c ON p.CategoryID = c.CategoryID "
                + "WHERE YEARWEEK(o.OrderDateTime, 1) = YEARWEEK(CURDATE(), 1) " // Lọc theo tuần hiện tại
                + "GROUP BY c.CategoryID, c.Name "
                + "ORDER BY SUM(od.Quantity) DESC LIMIT 1";

        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                categoryName = rs.getString("Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryName;
    }

    // 4. Số lượng khách hàng mới trong tháng này
    public int getNewCustomersThisMonth() {
        int count = 0;
        // Tên cột đúng là RegistrationDate thay vì CreateDate
        String sql = "SELECT COUNT(*) FROM Customer "
                + "WHERE MONTH(RegistrationDate) = MONTH(CURDATE()) "
                + "AND YEAR(RegistrationDate) = YEAR(CURDATE())";
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
        String sql = "SELECT SUM(TotalAmount) FROM `Order` "
                + "WHERE OrderDateTime >= DATE_SUB(NOW(), INTERVAL 60 DAY) "
                + "AND OrderDateTime < DATE_SUB(NOW(), INTERVAL 30 DAY)";
        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

// 2. Lấy số đơn hàng của ngày hôm qua
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

// 3. Lấy số khách hàng mới của tháng trước
    public int getNewCustomersLastMonth() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM Customer "
                + "WHERE RegistrationDate >= DATE_SUB(DATE_FORMAT(NOW() ,'%Y-%m-01'), INTERVAL 1 MONTH) "
                + "AND RegistrationDate < DATE_FORMAT(NOW() ,'%Y-%m-01')";
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
