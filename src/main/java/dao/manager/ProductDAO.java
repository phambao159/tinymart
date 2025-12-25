package dao.manager;

import model.manager.ProductSummary;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;

public class ProductDAO {

    public List<ProductSummary> getProductSummaries() {
        List<ProductSummary> productSummaries = new ArrayList<>();

        String sql = "SELECT "
                + "P.ProductID, P.Name, P.CategoryID, P.Unit, P.Image, P.Status,"
                + "MIN(PS.CostPrice) AS MinCostPrice, "
                + "MIN(PS.SellingPrice) AS MinSellingPrice, "
                + "SUM(PS.StockQuantity) AS TotalStockQuantity "
                + "FROM Product P "
                + "LEFT JOIN ProductSize PS ON P.ProductID = PS.ProductID "
                + "GROUP BY P.ProductID, P.Name, P.CategoryID, P.Unit, P.Image "
                + "ORDER BY P.ProductID";

        try {
            DBConnection dc = new DBConnection();
            try (Connection conn = dc.getConnect(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Double minSellingPrice = rs.getDouble("MinSellingPrice");
                    Long totalStockQuantity = rs.getLong("TotalStockQuantity");

                    if (rs.wasNull()) {
                        minSellingPrice = 0.0;
                        totalStockQuantity = 0L;
                    }

                    ProductSummary summary = new ProductSummary(
                            rs.getInt("ProductID"),
                            rs.getString("Name"),
                            rs.getInt("CategoryID"),
                            rs.getString("Unit"),
                            rs.getString("Image"),
                            minSellingPrice,
                            totalStockQuantity,
                            rs.getString("Status")
                    );
                    productSummaries.add(summary);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error when fetching product summaries: " + e.getMessage());
            e.printStackTrace();
        }
        return productSummaries;
    }

}
