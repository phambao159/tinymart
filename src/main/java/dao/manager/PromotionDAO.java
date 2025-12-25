package dao.manager;

import model.manager.Promotion; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection; 


public class PromotionDAO {


    public List<Promotion> getData() {
        List<Promotion> promotions = new ArrayList<>();


        String sql = "SELECT PromotionID, Name, Description, Type, Value, StartDate, EndDate, Status FROM Promotion";

        try {
            DBConnection dc = new DBConnection();
            try (Connection conn = dc.getConnect();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    java.sql.Date sqlStartDate = rs.getDate("StartDate");
                    java.sql.Date sqlEndDate = rs.getDate("EndDate");
                    
                    Promotion promotion = new Promotion(
                        rs.getInt("PromotionID"),
                        rs.getString("Name"),
                        rs.getString("Description"), 
                        rs.getString("Type"),       
                        rs.getDouble("Value"),     
                        sqlStartDate != null ? sqlStartDate.toLocalDate() : null,
                        sqlEndDate != null ? sqlEndDate.toLocalDate() : null,
                        rs.getString("Status")
                    );
                    promotions.add(promotion);
                }
            } 

        } catch (SQLException e) {
            System.err.println("SQL Error when fetching promotions: " + e.getMessage());
            e.printStackTrace();
        }
        return promotions;
    }
    

}