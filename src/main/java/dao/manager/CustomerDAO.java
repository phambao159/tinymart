package dao.manager;

import model.manager.Customer;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private final DBConnection dc = new DBConnection();

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM Customer";
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Customer(
                    rs.getInt("CustomerID"),
                    rs.getString("FullName"),
                    rs.getString("PhoneNumber"),
                    rs.getInt("Points"),
                    rs.getString("Email"),
                    rs.getDate("RegistrationDate").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addCustomer(Customer c) {
        String sql = "INSERT INTO Customer (FullName, PhoneNumber, Points, Email, RegistrationDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getFullName());
            pstmt.setString(2, c.getPhoneNumber());
            pstmt.setInt(3, c.getPoints());
            pstmt.setString(4, c.getEmail());
            pstmt.setDate(5, Date.valueOf(c.getRegistrationDate()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}