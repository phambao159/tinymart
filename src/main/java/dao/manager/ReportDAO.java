package dao.manager;

import model.manager.Report;
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
            sql = "SELECT DATE(OrderDateTime) as DateLabel, SUM(TotalAmount) as Total " +
                  "FROM `Order` " +
                  "WHERE OrderDateTime >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                  "GROUP BY DATE(OrderDateTime) ORDER BY DateLabel ASC";
            break;

        case "Last Month":
            sql = "SELECT DATE(OrderDateTime) as DateLabel, SUM(TotalAmount) as Total " +
                  "FROM `Order` " +
                  "WHERE OrderDateTime >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) " +
                  "GROUP BY DATE(OrderDateTime) ORDER BY DateLabel ASC";
            break;

        case "Last Year":
            sql = "SELECT DATE_FORMAT(OrderDateTime, '%Y-%m') as DateLabel, SUM(TotalAmount) as Total " +
                  "FROM `Order` " +
                  "WHERE OrderDateTime >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
                  "GROUP BY DateLabel ORDER BY DateLabel ASC";
            break;

        default:
            if (filter.startsWith("Month")) {
                String monthNum = filter.replace("Month ", "").trim();
                sql = "SELECT DATE(OrderDateTime) as DateLabel, SUM(TotalAmount) as Total " +
                      "FROM `Order` " +
                      "WHERE MONTH(OrderDateTime) = " + monthNum + " AND YEAR(OrderDateTime) = YEAR(CURDATE()) " +
                      "GROUP BY DATE(OrderDateTime) ORDER BY DateLabel ASC";
            } else {
                // Mặc định nếu không khớp cái nào
                sql = "SELECT DATE(OrderDateTime) as DateLabel, SUM(TotalAmount) as Total FROM `Order` GROUP BY DATE(OrderDateTime) LIMIT 15";
            }
            break;
    }

    try (Connection conn = dc.getConnect();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        
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
        String sql = "SELECT p.Name, SUM(od.Quantity) as TotalSold " +
                     "FROM `Order Detail` od " +
                     "JOIN Product p ON od.ProductID = p.ProductID " +
                     "GROUP BY p.ProductID " +
                     "ORDER BY TotalSold DESC LIMIT 10";
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Report(rs.getString("Name"), rs.getDouble("TotalSold")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}