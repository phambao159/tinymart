package dao.manager;

import model.manager.Category; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;


public class CategoryDAO {


    public List<Category> getData() {

        List<Category> categories = new ArrayList<>();


        String sql = "SELECT CategoryID, Name, Description FROM Category";

        try {
        
            DBConnection dc = new DBConnection();
            try (Connection conn = dc.getConnect();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Category category = new Category(
                        rs.getInt("CategoryID"),         
                        rs.getString("Name"),            
                        rs.getString("Description")     
                    );
                    categories.add(category);
                }
            } 

        } catch (SQLException e) {
            System.err.println("SQL Error when fetching categories: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

}