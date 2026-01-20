package dao.manager.report;

import model.manager.report.Report;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap; // PHẢI CÓ DÒNG NÀY
import java.util.Map;     // PHẢI CÓ DÒNG NÀY


public class ReportDAO {

    private final DBConnection dc = new DBConnection();

    /**

     * HÀM TỔNG LỰC: Lấy tất cả thông số Dashboard bằng Map
     */
    /**
     * HÀM TỔNG LỰC: Lấy tất cả thông số Dashboard trong 1 lần kết nối duy nhất.
     * Đổ dữ liệu vào Constructor mới của Model Report.
     */
    public Report getDashboardData() {
        // Câu lệnh SQL gom tất cả các số liệu vào một lần SELECT   
        String sql = "SELECT "
                + "(SELECT COALESCE(SUM(TotalAmount), 0) FROM `Order` WHERE OrderDateTime >= DATE_SUB(NOW(), INTERVAL 30 DAY)) as rev30, "
                + "(SELECT COALESCE(SUM(TotalAmount), 0) FROM `Order` WHERE OrderDateTime >= DATE_SUB(NOW(), INTERVAL 60 DAY) AND OrderDateTime < DATE_SUB(NOW(), INTERVAL 30 DAY)) as revPrev30, "
                + "(SELECT COUNT(*) FROM `Order` WHERE DATE(OrderDateTime) = CURDATE()) as ordersToday, "
                + "(SELECT COUNT(*) FROM `Order` WHERE DATE(OrderDateTime) = SUBDATE(CURDATE(), 1)) as ordersYesterday, "
                + "(SELECT COUNT(*) FROM Customer WHERE MONTH(RegistrationDate) = MONTH(CURDATE()) AND YEAR(RegistrationDate) = YEAR(CURDATE())) as custMonth, "
                + "(SELECT COUNT(*) FROM Customer WHERE RegistrationDate >= DATE_SUB(DATE_FORMAT(NOW() ,'%Y-%m-01'), INTERVAL 1 MONTH) AND RegistrationDate < DATE_FORMAT(NOW() ,'%Y-%m-01')) as custPrevMonth, "
                + "(SELECT c.Name FROM OrderDetail od JOIN `Order` o ON od.OrderID = o.OrderID "
                + " JOIN ProductSize ps ON od.ProductSizeID = ps.ProductSizeID JOIN Product p ON ps.ProductID = p.ProductID "
                + " JOIN Category c ON p.CategoryID = c.CategoryID WHERE YEARWEEK(o.OrderDateTime, 1) = YEARWEEK(CURDATE(), 1) "
                + " GROUP BY c.CategoryID, c.Name ORDER BY SUM(od.Quantity) DESC LIMIT 1) as topCat";

        try (Connection con = dc.getConnect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                // Trả về đối tượng Report sử dụng Constructor Dashboard
                return new Report(
                        rs.getDouble("rev30"),
                        rs.getDouble("revPrev30"),
                        rs.getInt("ordersToday"),
                        rs.getInt("ordersYesterday"),
                        rs.getInt("custMonth"),
                        rs.getInt("custPrevMonth"),
                        rs.getString("topCat")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy dữ liệu biểu đồ doanh thu theo Filter
     */
    public List<Report> getRevenueByFilter(String filter) {
        List<Report> list = new ArrayList<>();
        String sql = buildRevenueSql(filter);

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

     * Lấy Top 10 sản phẩm bán chạy
     */
    public List<Report> getTopSellingProducts(String filter) {
        List<Report> list = new ArrayList<>();
        String whereClause = buildWhereClause(filter);

        String sql = "SELECT p.Name, SUM(od.Quantity) AS TotalSold "
                + "FROM OrderDetail od JOIN `Order` o ON od.OrderID = o.OrderID "
                + "JOIN ProductSize ps ON od.ProductSizeID = ps.ProductSizeID JOIN Product p ON ps.ProductID = p.ProductID "
                + whereClause
                + " GROUP BY p.ProductID, p.Name ORDER BY TotalSold DESC LIMIT 10";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Report(rs.getString("Name"), rs.getDouble("TotalSold")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    // --- CÁC HÀM BỔ TRỢ ---
    private String buildRevenueSql(String filter) {
        if (filter.startsWith("Year ")) {
            String year = filter.replace("Year ", "").trim();
            return "SELECT DATE_FORMAT(OrderDateTime, '%m/%Y') as DateLabel, SUM(TotalAmount) as Total FROM `Order` WHERE YEAR(OrderDateTime) = " + year + " GROUP BY MONTH(OrderDateTime) ORDER BY MONTH(OrderDateTime) ASC";
        } else if (filter.contains(" - Month ")) {
            String[] parts = filter.split(" - Month ");
            return "SELECT DATE_FORMAT(OrderDateTime, '%d/%m') as DateLabel, SUM(TotalAmount) as Total FROM `Order` WHERE YEAR(OrderDateTime) = " + parts[0] + " AND MONTH(OrderDateTime) = " + parts[1] + " GROUP BY DATE(OrderDateTime) ORDER BY OrderDateTime ASC";
        } else if (filter.contains(" - Quarter ")) {
            String[] parts = filter.split(" - Quarter ");
            return "SELECT DATE_FORMAT(OrderDateTime, '%m/%Y') as DateLabel, SUM(TotalAmount) as Total FROM `Order` WHERE YEAR(OrderDateTime) = " + parts[0] + " AND QUARTER(OrderDateTime) = " + parts[1] + " GROUP BY MONTH(OrderDateTime) ORDER BY MONTH(OrderDateTime) ASC";
        }
        return "";
    }

    private String buildWhereClause(String filter) {
        String where = " WHERE o.OrderDateTime <= NOW() ";

        if (filter.startsWith("Year ")) {
            // Cắt "Year 2026" lấy "2026"
            String year = filter.replace("Year ", "").trim();
            where += " AND YEAR(o.OrderDateTime) = " + year;
        } else if (filter.contains(" - Month ")) {
            // Cắt "2026 - Month 1" -> parts[0]="2026", parts[1]="1"
            String[] parts = filter.split(" - Month ");
            where += " AND YEAR(o.OrderDateTime) = " + parts[0].trim()
                    + " AND MONTH(o.OrderDateTime) = " + parts[1].trim();
        } else if (filter.contains(" - Quarter ")) {
            // Cắt "2026 - Quarter 1" -> parts[0]="2026", parts[1]="1"
            String[] parts = filter.split(" - Quarter ");
            where += " AND YEAR(o.OrderDateTime) = " + parts[0].trim()
                    + " AND QUARTER(o.OrderDateTime) = " + parts[1].trim();
        }
        return where;
    }

    // Đừng quên hàm này cho Top Category nếu bạn chưa gộp vào SQL tổng
    public String getTopCategoryThisWeek() {
        String categoryName = "N/A";
        String sql = "SELECT c.Name FROM OrderDetail od JOIN `Order` o ON od.OrderID = o.OrderID "
                + "JOIN ProductSize ps ON od.ProductSizeID = ps.ProductSizeID JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Category c ON p.CategoryID = c.CategoryID WHERE YEARWEEK(o.OrderDateTime, 1) = YEARWEEK(CURDATE(), 1) "
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
}
