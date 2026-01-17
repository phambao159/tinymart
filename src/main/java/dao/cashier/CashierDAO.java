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
                + "COALESCE(SUM(id.ShelfQuantity), 0) as TotalStock "
                + "FROM product p "
                + "JOIN productsize ps ON p.ProductID = ps.ProductID "
                + "LEFT JOIN ImportDetail id ON ps.ProductSizeID = id.ProductSizeID "
                + "WHERE p.Status = 'Active' "
                + "AND (p.Name LIKE ? OR p.ProductID LIKE ?) "
                + "GROUP BY p.ProductID, p.Name, p.Image, p.CategoryID";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Product(
                        String.valueOf(rs.getInt("ProductID")),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getString("Image"),
                        rs.getInt("CategoryID"),
                        rs.getInt("TotalStock")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductSizeInfo> getProductSizes(int productId) {
        List<ProductSizeInfo> list = new ArrayList<>();

        String sql = "SELECT ps.SizeID, s.Type, ps.SellingPrice, "
                + "COALESCE(SUM(id.ShelfQuantity), 0) as CurrentStock "
                + "FROM productsize ps "
                + "JOIN size s ON ps.SizeID = s.SizeID "
                + "LEFT JOIN ImportDetail id ON ps.ProductSizeID = id.ProductSizeID "
                + "WHERE ps.ProductID = ? "
                + "GROUP BY ps.SizeID, s.Type, ps.SellingPrice";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new ProductSizeInfo(
                        rs.getInt("SizeID"),
                        rs.getString("Type"),
                        rs.getDouble("SellingPrice"),
                        rs.getInt("CurrentStock")
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

    public List<OrderViewModel> getOrderHistory() {
        List<OrderViewModel> list = new ArrayList<>();
        String sql = "SELECT o.OrderID, o.OrderDateTime, "
                + "e.FullName AS CashierName, "
                + "COALESCE(c.FullName, 'Guest') AS CustomerName, "
                + "COALESCE(c.PhoneNumber, '') AS PhoneNumber, "
                + "COALESCE(c.Points, 0) AS Points, "
                + "o.TotalAmount, o.PaymentMethod "
                + "FROM `Order` o "
                + "JOIN Employee e ON o.EmployeeID = e.EmployeeID "
                + "LEFT JOIN Customer c ON o.CustomerID = c.CustomerID "
                + "ORDER BY o.OrderID DESC";

        try (Connection conn = dc.getConnect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new OrderViewModel(
                        rs.getInt("OrderID"),
                        rs.getString("OrderDateTime"),
                        rs.getString("CashierName"),
                        rs.getString("CustomerName"),
                        rs.getString("PhoneNumber"),
                        rs.getInt("Points"),
                        rs.getDouble("TotalAmount"),
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
                + "COALESCE(c.Points, 0) AS Points,"
                + "o.TotalAmount, o.PaymentMethod "
                + "FROM `Order` o "
                + "JOIN Employee e ON o.EmployeeID = e.EmployeeID "
                + "LEFT JOIN Customer c ON o.CustomerID = c.CustomerID "
                + "WHERE 1=1 "

        );

        if (date != null) {
            sql.append(" AND DATE(o.OrderDateTime) = ? ");
        }

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (c.FullName LIKE ? OR c.PhoneNumber LIKE ?) ");
        }

        sql.append("ORDER BY o.OrderID DESC");

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
                    list.add(new OrderViewModel(
                            rs.getInt("OrderID"),
                            rs.getString("OrderDateTime"),
                            rs.getString("CashierName"),
                            rs.getString("CustomerName"),
                            rs.getString("PhoneNumber"),
                            rs.getInt("Points"),
                            rs.getDouble("TotalAmount"),
                            rs.getString("PaymentMethod")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int createOrder(int employeeId, Integer customerId, double totalAmount, double discount, String paymentMethod, List<model.cashier.CartItem> cartItems) {
        int generatedOrderId = -1;
        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psDetail = null;
        ResultSet rs = null;

        try {
            conn = dc.getConnect();
            conn.setAutoCommit(false);

            String sqlOrder = "INSERT INTO `Order` (OrderDateTime, EmployeeID, CustomerID, TotalAmount, DiscountAmount, PaymentMethod) VALUES (NOW(), ?, ?, ?, ?, ?)";
            psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);

            psOrder.setInt(1, employeeId);
            if (customerId != null) {
                psOrder.setInt(2, customerId);
            } else {
                psOrder.setNull(2, java.sql.Types.INTEGER);
            }
            psOrder.setDouble(3, totalAmount);
            psOrder.setDouble(4, discount);
            psOrder.setString(5, paymentMethod);

            int affectedRows = psOrder.executeUpdate();

            if (affectedRows > 0) {
                rs = psOrder.getGeneratedKeys();
                if (rs.next()) {
                    generatedOrderId = rs.getInt(1);
                }
            } else {
                conn.rollback();
                return -1;
            }

            String sqlDetail = "INSERT INTO OrderDetail (OrderID, ProductSizeID, Quantity) VALUES (?, ?, ?)";
            psDetail = conn.prepareStatement(sqlDetail);

            for (model.cashier.CartItem item : cartItems) {
                psDetail.setInt(1, generatedOrderId);
                int productSizeId = getProductSizeID(conn, Integer.parseInt(item.getProductId()), item.getSizeId());

                psDetail.setInt(2, productSizeId);
                psDetail.setInt(3, item.getQuantity());
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
        String sql = "SELECT p.ProductID, p.Name, s.Type, od.Quantity, ps.SellingPrice "
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
                list.add(new model.cashier.CartItem(
                        String.valueOf(rs.getInt("ProductID")),
                        0,
                        fullName,
                        rs.getInt("Quantity"),
                        rs.getDouble("SellingPrice")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
