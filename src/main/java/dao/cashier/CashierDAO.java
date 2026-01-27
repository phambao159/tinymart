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
import model.cashier.OrderViewModel;

public class CashierDAO {

    DBConnection dc = new DBConnection();

    public List<Product> getAllProducts() {
        return searchProducts("");
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();

        String sql = "SELECT p.ProductID, p.Name, p.Image, p.CategoryID, "
                + "MIN(ps.SellingPrice) as Price, "
                + "COALESCE(SUM(id.ShelfQuantity), 0) as TotalStock, "
                + "MAX(CASE "
                + "WHEN promo.PromotionID IS NOT NULL "
                + "AND promo.Status = 'Active' "
                + "AND CURDATE() BETWEEN promo.StartDate AND promo.EndDate "
                + "THEN 1 ELSE 0 "
                + "END) as HasPromo "
                + "FROM product p "
                + "JOIN productsize ps ON p.ProductID = ps.ProductID "
                + "LEFT JOIN ImportDetail id ON ps.ProductSizeID = id.ProductSizeID "
                + "LEFT JOIN Promotion promo ON ps.PromotionID = promo.PromotionID "
                + "WHERE p.Status = 'Active' "
                + "AND (p.Name LIKE ? OR p.ProductID LIKE ?) "
                + "GROUP BY p.ProductID, p.Name, p.Image, p.CategoryID";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product p = new Product(
                        String.valueOf(rs.getInt("ProductID")),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getString("Image"),
                        rs.getInt("CategoryID"),
                        rs.getInt("TotalStock")
                );
                p.setHasPromo(rs.getInt("HasPromo") == 1);

                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductSizeInfo> getProductSizes(int productId) {
        List<ProductSizeInfo> list = new ArrayList<>();

        String sql = "SELECT ps.SizeID, s.Type, ps.SellingPrice, "
                + "COALESCE(SUM(id.ShelfQuantity), 0) as CurrentStock, "
                + "p.Type as PromoType, p.Value as PromoValue, p.Description as PromoDesc "
                + "FROM productsize ps "
                + "JOIN size s ON ps.SizeID = s.SizeID "
                + "LEFT JOIN ImportDetail id ON ps.ProductSizeID = id.ProductSizeID "
                + "LEFT JOIN Promotion p ON ps.PromotionID = p.PromotionID "
                + "AND p.Status = 'Active' "
                + "AND CURDATE() BETWEEN p.StartDate AND p.EndDate "
                + "WHERE ps.ProductID = ? "
                + "GROUP BY ps.SizeID, s.Type, ps.SellingPrice, p.Type, p.Value, p.Description";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new ProductSizeInfo(
                        rs.getInt("SizeID"),
                        rs.getString("Type"),
                        rs.getDouble("SellingPrice"),
                        rs.getInt("CurrentStock"),
                        rs.getString("PromoType"),
                        rs.getDouble("PromoValue"),
                        rs.getString("PromoDesc")
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
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addCustomer(Customer c) {
        String sql = "INSERT INTO Customer (FullName, PhoneNumber, Email, Points, RegistrationDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getFullName());
            pstmt.setString(2, c.getPhoneNumber());
            pstmt.setString(3, c.getEmail());
            pstmt.setInt(4, 0);
            pstmt.setDate(5, Date.valueOf(LocalDate.now()));
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateCustomerPoints(int customerId, int newTotalPoints) {
        String sql = "UPDATE Customer SET Points = ? WHERE CustomerID = ?";
        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newTotalPoints);
            pstmt.setInt(2, customerId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean reduceStock(int productId, int sizeId, int quantitySold) {
        Connection conn = null;
        try {
            conn = dc.getConnect();
            conn.setAutoCommit(false);
            String sqlGetBatches = "SELECT ImportDetailID, ShelfQuantity FROM ImportDetail "
                    + "WHERE ProductSizeID = (SELECT ProductSizeID FROM ProductSize WHERE ProductID = ? AND SizeID = ?) "
                    + "AND ShelfQuantity > 0 "
                    + "ORDER BY ExpiryDate ASC";

            try (PreparedStatement psGet = conn.prepareStatement(sqlGetBatches)) {
                psGet.setInt(1, productId);
                psGet.setInt(2, sizeId);

                try (ResultSet rs = psGet.executeQuery()) {
                    int qtyNeedDeduct = quantitySold;

                    while (rs.next() && qtyNeedDeduct > 0) {
                        int batchId = rs.getInt("ImportDetailID");
                        int currentShelfQty = rs.getInt("ShelfQuantity");
                        int deductAmount = Math.min(currentShelfQty, qtyNeedDeduct);
                        String sqlUpdateBatch = "UPDATE ImportDetail SET ShelfQuantity = ShelfQuantity - ? WHERE ImportDetailID = ?";
                        try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateBatch)) {
                            psUpdate.setInt(1, deductAmount);
                            psUpdate.setInt(2, batchId);
                            psUpdate.executeUpdate();
                        }

                        qtyNeedDeduct -= deductAmount;
                    }
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public List<OrderViewModel> getOrderHistory(String keyword) {
        List<OrderViewModel> list = new ArrayList<>();
        String sql = "SELECT o.OrderID, o.OrderDateTime, e.FullName as CashierName, c.FullName as CustomerName, "
                + "c.PhoneNumber, c.Points, o.TotalAmount, "
                + "o.PointDiscount, "
                + "o.DiscountAmount AS TotalDiscount, "
                + "p.PaymentMethod "
                + "FROM `Order` o "
                + "LEFT JOIN Employee e ON o.EmployeeID = e.EmployeeID "
                + "LEFT JOIN Customer c ON o.CustomerID = c.CustomerID "
                + "JOIN Payment p ON o.PaymentID = p.PaymentID "
                + "WHERE (c.PhoneNumber LIKE ? OR o.OrderID LIKE ?) "
                + "ORDER BY o.OrderDateTime DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String fullDateTime = rs.getString("OrderDateTime");
                if (fullDateTime != null && fullDateTime.endsWith(".0")) {
                    fullDateTime = fullDateTime.substring(0, fullDateTime.length() - 2);
                }
                list.add(new OrderViewModel(
                        rs.getInt("OrderID"),
                        fullDateTime,
                        rs.getString("CashierName"),
                        rs.getString("CustomerName"),
                        rs.getString("PhoneNumber"),
                        rs.getInt("Points"),
                        rs.getDouble("TotalAmount"),
                        rs.getDouble("TotalDiscount"),
                        rs.getDouble("PointDiscount"),
                        rs.getString("PaymentMethod")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<OrderViewModel> searchOrderHistory(LocalDate date, String keyword) {
        List<OrderViewModel> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT o.OrderID, o.OrderDateTime, "
                + "e.FullName AS CashierName, "
                + "COALESCE(c.FullName, 'Guest') AS CustomerName, "
                + "COALESCE(c.PhoneNumber, '') AS PhoneNumber, "
                + "COALESCE(c.Points, 0) AS Points, "
                + "o.TotalAmount, "
                + "o.DiscountAmount AS TotalDiscount, "
                + "o.PointDiscount, "
                + "o.PaymentMethod "
                + "FROM `Order` o "
                + "JOIN Employee e ON o.EmployeeID = e.EmployeeID "
                + "LEFT JOIN Customer c ON o.CustomerID = c.CustomerID "
                + "WHERE o.OrderDateTime <= NOW() ");

        if (date != null) {
            sql.append(" AND DATE(o.OrderDateTime) = ? ");
        }

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (c.FullName LIKE ? OR c.PhoneNumber LIKE ?) ");
        }

        sql.append(" ORDER BY o.OrderDateTime DESC");

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int index = 1;

            if (date != null) {
                pstmt.setDate(index++, java.sql.Date.valueOf(date));
            }

            if (keyword != null && !keyword.isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                pstmt.setString(index++, searchPattern);
                pstmt.setString(index++, searchPattern);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String fullTime = rs.getString("OrderDateTime");
                    if (fullTime != null && fullTime.endsWith(".0")) {
                        fullTime = fullTime.substring(0, fullTime.length() - 2);
                    }

                    list.add(new OrderViewModel(
                            rs.getInt("OrderID"),
                            fullTime,
                            rs.getString("CashierName"),
                            rs.getString("CustomerName"),
                            rs.getString("PhoneNumber"),
                            rs.getInt("Points"),
                            rs.getDouble("TotalAmount"),
                            rs.getDouble("TotalDiscount"),
                            rs.getDouble("PointDiscount"),
                            rs.getString("PaymentMethod")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int createOrder(int employeeId, Integer customerId, double totalAmount, double totalDiscount, double pointDiscount, String paymentMethod, List<model.cashier.CartItem> cartItems) {
        int generatedOrderId = -1;
        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psDetail = null;
        ResultSet rs = null;

        try {
            conn = dc.getConnect();
            conn.setAutoCommit(false);

            String sqlOrder = "INSERT INTO `Order` (OrderDateTime, EmployeeID, CustomerID, TotalAmount, DiscountAmount, PointDiscount, PaymentMethod) VALUES (NOW(), ?, ?, ?, ?, ?, ?)";
            psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, employeeId);
            if (customerId != null) {
                psOrder.setInt(2, customerId);
            } else {
                psOrder.setNull(2, java.sql.Types.INTEGER);
            }
            psOrder.setDouble(3, totalAmount);
            psOrder.setDouble(4, totalDiscount);
            psOrder.setDouble(5, pointDiscount);
            psOrder.setString(6, paymentMethod);

            if (psOrder.executeUpdate() > 0) {
                rs = psOrder.getGeneratedKeys();
                if (rs.next()) {
                    generatedOrderId = rs.getInt(1);
                }
            } else {
                conn.rollback();
                return -1;
            }

            String sqlDetail = "INSERT INTO OrderDetail (OrderID, ProductSizeID, Quantity, original_price, selling_price, unit_cost) VALUES (?, ?, ?, ?, ?, ?)";
            psDetail = conn.prepareStatement(sqlDetail);

            for (model.cashier.CartItem item : cartItems) {
                psDetail.setInt(1, generatedOrderId);
                int productSizeId = getProductSizeID(conn, Integer.parseInt(item.getProductId()), item.getSizeId());
                psDetail.setInt(2, productSizeId);
                psDetail.setInt(3, item.getQuantity());

                double originalPrice = item.getPrice();
                double sellingPrice = item.getSellingPrice();
                double unitCost = getCurrentCostPrice(conn, productSizeId);

                psDetail.setDouble(4, originalPrice);
                psDetail.setDouble(5, sellingPrice);
                psDetail.setDouble(6, unitCost);

                psDetail.addBatch();
            }
            psDetail.executeBatch();
            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
            }
            generatedOrderId = -1;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (psOrder != null) {
                    psOrder.close();
                }
                if (psDetail != null) {
                    psDetail.close();
                }
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
            }
        }
        return generatedOrderId;
    }

    private int getProductSizeID(Connection conn, int productId, int sizeId) throws SQLException {
        String sql = "SELECT ProductSizeID FROM ProductSize WHERE ProductID = ? AND SizeID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, sizeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ProductSizeID");
                }
            }
        }
        return 0;
    }

    public int getNextOrderId() {
        int nextId = 1;
        String sql = "SELECT MAX(OrderID) FROM `Order`";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextId;
    }

    public List<model.cashier.CartItem> getOrderDetails(int orderId) {
        List<model.cashier.CartItem> list = new ArrayList<>();
        String sql = "SELECT p.ProductID, p.Name, s.Type, od.Quantity, "
                + "CASE "
                + "   WHEN od.selling_price > 0 THEN od.selling_price "
                + "   ELSE ps.SellingPrice "
                + "END AS FinalPrice "
                + "FROM OrderDetail od "
                + "JOIN ProductSize ps ON od.ProductSizeID = ps.ProductSizeID "
                + "JOIN Product p ON ps.ProductID = p.ProductID "
                + "JOIN Size s ON ps.SizeID = s.SizeID "
                + "WHERE od.OrderID = ?";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String fullName = rs.getString("Name") + " (" + rs.getString("Type") + ")";
                double finalPrice = rs.getDouble("FinalPrice");
                list.add(new model.cashier.CartItem(
                        String.valueOf(rs.getInt("ProductID")),
                        0,
                        fullName,
                        rs.getInt("Quantity"),
                        finalPrice,
                        null,
                        0.0
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private double getCurrentCostPrice(Connection conn, int productSizeId) {
        String sql = "SELECT ImportPrice FROM ImportDetail WHERE ProductSizeID = ? AND ShelfQuantity > 0 ORDER BY ExpiryDate ASC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productSizeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("ImportPrice");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public java.util.Map<String, String> getShiftStatistics(int employeeId) {
        java.util.Map<String, String> stats = new java.util.HashMap<>();

        stats.put("StartTime", "N/A");
        stats.put("TotalOrders", "0");
        stats.put("TotalRevenue", "0.00");
        stats.put("CashRevenue", "0.00");
        stats.put("StartCash", "0.0");

        Connection conn = null;
        Timestamp checkInTime = null;

        try {
            conn = dc.getConnect();

            String sqlShift = "SELECT CheckInTime, StartCash FROM EmployeeShift "
                    + "WHERE EmployeeID = ? AND CheckOutTime IS NULL "
                    + "ORDER BY CheckInTime DESC LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(sqlShift)) {
                ps.setInt(1, employeeId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double startCash = rs.getDouble("StartCash");
                    stats.put("StartCash", String.valueOf(startCash));

                    Timestamp dbTime = rs.getTimestamp("CheckInTime");

                    java.util.Calendar now = java.util.Calendar.getInstance();
                    java.util.Calendar checkInCal = java.util.Calendar.getInstance();
                    checkInCal.setTimeInMillis(dbTime.getTime());

                    now.set(java.util.Calendar.HOUR_OF_DAY, checkInCal.get(java.util.Calendar.HOUR_OF_DAY));
                    now.set(java.util.Calendar.MINUTE, checkInCal.get(java.util.Calendar.MINUTE));
                    now.set(java.util.Calendar.SECOND, checkInCal.get(java.util.Calendar.SECOND));

                    checkInTime = new Timestamp(now.getTimeInMillis());

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                    stats.put("StartTime", sdf.format(checkInTime));
                }
            }

            if (checkInTime == null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);

                checkInTime = new Timestamp(cal.getTimeInMillis());

                stats.put("StartTime", "Today (No Check-in)");
                stats.put("StartCash", "0.0");
            }

            String sqlStats = "SELECT "
                    + "COUNT(*) as TotalOrd, "
                    + "SUM(TotalAmount) as TotalRev, "
                    + "SUM(CASE WHEN PaymentMethod = 'Cash' THEN TotalAmount ELSE 0 END) as CashRev "
                    + "FROM `Order` "
                    + "WHERE EmployeeID = ? AND OrderDateTime >= ?";

            try (PreparedStatement ps2 = conn.prepareStatement(sqlStats)) {
                ps2.setInt(1, employeeId);
                ps2.setTimestamp(2, checkInTime);

                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) {
                    stats.put("TotalOrders", String.valueOf(rs2.getInt("TotalOrd")));

                    double rev = rs2.getDouble("TotalRev");
                    stats.put("TotalRevenue", String.format("%.2f", rev));

                    double cashRev = rs2.getDouble("CashRev");
                    stats.put("CashRevenue", String.valueOf(cashRev));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return stats;
    }

    public String getCurrentShiftName(int employeeId) {
        String shiftName = "Not in Shift";

        Connection conn = null;
        try {
            conn = dc.getConnect();
            String sql = "SELECT s.ShiftName "
                    + "FROM EmployeeShift es "
                    + "JOIN Shift s ON es.ShiftID = s.ShiftID "
                    + "WHERE es.EmployeeID = ? "
                    + "AND es.WorkDate = CURDATE() "
                    + "AND CURTIME() BETWEEN s.StartTime AND s.EndTime";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, employeeId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    shiftName = rs.getString("ShiftName");
                }
            }

            if (shiftName.equals("Not in Shift")) {
                String sqlNext = "SELECT s.ShiftName FROM EmployeeShift es "
                        + "JOIN Shift s ON es.ShiftID = s.ShiftID "
                        + "WHERE es.EmployeeID = ? AND es.WorkDate = CURDATE() "
                        + "ORDER BY s.StartTime ASC LIMIT 1";

                try (PreparedStatement ps2 = conn.prepareStatement(sqlNext)) {
                    ps2.setInt(1, employeeId);
                    ResultSet rs2 = ps2.executeQuery();
                    if (rs2.next()) {
                        shiftName = rs2.getString("ShiftName");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return shiftName;
    }

    public int getAssignedShiftID(int employeeId) {
        int shiftId = 0; 
        String sql = "SELECT ShiftID FROM EmployeeShift WHERE EmployeeID = ? AND WorkDate = CURDATE() AND CheckInTime IS NULL LIMIT 1";

        try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                shiftId = rs.getInt("ShiftID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shiftId;
    }
}
