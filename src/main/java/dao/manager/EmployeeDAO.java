package dao.manager;

import model.manager.Employee;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;

public class EmployeeDAO {


    DBConnection dc = new DBConnection();

    public String authenticate(String username, String password) throws Exception {
        

        String sql = "SELECT * FROM Employee WHERE user = ? AND password = ?";
        
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Login success!");
                    return rs.getString(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());

            throw new Exception("Lỗi truy vấn cơ sở dữ liệu.", e);
        }
        
        return null; 
    }
    public List<Employee> getData() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM Employee";
        try (Connection conn = dc.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Employee(
                    rs.getInt("EmployeeID"),
                    rs.getString("FullName"),
                    rs.getDate("DateOfBirth").toLocalDate(),
                    rs.getString("PhoneNumber"),
                    rs.getString("Address"),
                    rs.getString("Role"),
                    rs.getDate("HireDate").toLocalDate(),
                    rs.getLong("BaseSalary"),
                    rs.getString("User"),
                    rs.getString("Password")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    

  
}