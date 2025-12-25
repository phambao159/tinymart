/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author user
 */
public class DBConnection {
     Connection con;

    public Connection getConnect() {
        String url = "jdbc:mysql://localhost:3306/tinymart";
        String user = "root";
        String password = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Connected successfully!");
        } catch (ClassNotFoundException ex) {
            System.out.println("Error ClassNotFoundException: " + ex.getMessage());
        } catch (SQLException ex) {
            System.out.println("Error SQLException: " + ex.getMessage());
        }
        return con;
    }
}
