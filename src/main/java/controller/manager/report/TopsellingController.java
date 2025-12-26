/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller.manager.report;

import dao.manager.ReportDAO;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import model.manager.Report;

/**
 * FXML Controller class
 *
 * @author user
 */
public class TopsellingController implements Initializable {
    private ReportDAO reportDAO = new ReportDAO();
    @FXML
    private PieChart pieChartProducts;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        drawTopProductsPieChart();
    }    
    private void drawTopProductsPieChart() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        List<Report> data = reportDAO.getTopSellingProducts();
        for (Report r : data) {
            String labelWithQty = String.format("%s (%.0f)", r.getLabel(), r.getValue());
            
            // Thêm vào danh sách dữ liệu của biểu đồ
            pieData.add(new PieChart.Data(labelWithQty, r.getValue()));
        }
        pieChartProducts.setData(pieData);
    }
    
}
