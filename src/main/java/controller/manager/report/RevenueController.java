package controller.manager.report;

import dao.manager.ReportDAO;
import model.manager.Report;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class RevenueController implements Initializable {

    @FXML private LineChart<String, Number> lineChartRevenue;
    @FXML private ComboBox<String> period;

    private ReportDAO reportDAO = new ReportDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBox();
        Platform.runLater(() -> loadData("Last 7 days"));
    }

    private void setupComboBox() {
        period.getItems().addAll("Last 7 days", "Last Month", "Last Year");
        for (int i = 1; i <= 12; i++) period.getItems().add("Month " + i);
        
        period.setValue("Last 7 days");
        period.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) loadData(newV);
        });
    }   

    private void loadData(String filterType) {
        // Xóa dữ liệu cũ trên biểu đồ
        lineChartRevenue.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue: " + filterType);

        // Gọi DAO lấy dữ liệu dựa trên filterType
        // Lưu ý: Bạn cần cập nhật hàm getRevenue(filterType) trong ReportDAO của mình
        List<Report> data = reportDAO.getRevenueByFilter(filterType);
        
        if (data == null || data.isEmpty()) return;

        for (Report r : data) {
            series.getData().add(new XYChart.Data<>(r.getLabel(), r.getValue()));
        }

        lineChartRevenue.getData().add(series);


        Platform.runLater(() -> addLabelsToSeries(series));
    }

    private void addLabelsToSeries(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> entry : series.getData()) {
            Node node = entry.getNode();
            if (node != null) {
                Label label = new Label(String.format("$%.2f", entry.getYValue().doubleValue()));
                label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 10px;");
                
                Group parent = (Group) node.getParent();
                if (!parent.getChildren().contains(label)) {
                    parent.getChildren().add(label);
                }

                label.layoutXProperty().bind(node.layoutXProperty().subtract(label.widthProperty().divide(2)));
                label.layoutYProperty().bind(node.layoutYProperty().subtract(20));
            }
        }
    }
}