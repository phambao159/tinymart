/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.manager.Size;
import util.DBConnection;

/**
 *
 * @author user
 */
public class SizeDAO {
    public List<Size> getData() {

        List<Size> sizes = new ArrayList<>();


        String sql = "SELECT SizeID,Type FROM Size";

        try {
        
            DBConnection dc = new DBConnection();
            try (Connection conn = dc.getConnect();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Size category = new Size(
                        rs.getInt("SizeID"),         
                        rs.getString("Type")   
                    );
                    sizes.add(category);
                }
            } 

        } catch (SQLException e) {
            System.err.println("SQL Error when fetching categories: " + e.getMessage());
            e.printStackTrace();
        }
        return sizes;
    }
}
