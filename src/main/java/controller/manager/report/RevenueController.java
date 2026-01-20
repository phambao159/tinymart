package controller.manager.report;

import dao.manager.report.ReportDAO;
import model.manager.report.Report;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalDate;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class RevenueController implements Initializable {

    @FXML private BarChart<String, Number> barChartRevenue;
    @FXML private ComboBox<String> periodyear;
    @FXML private ComboBox<String> periodmonth;

    private final ReportDAO reportDAO = new ReportDAO();
    private boolean isUpdating = false; // Biến cờ ngăn chặn gọi refresh liên tục

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
    }

    private void setupComboBoxes() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // 1. Nạp năm (từ 2023 đến nay)
        periodyear.getItems().clear();
        for (int y = 2023; y <= currentYear; y++) {
            periodyear.getItems().add(String.valueOf(y));
        }

        // 2. Lắng nghe thay đổi năm
        periodyear.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newYear) -> {
            if (newYear != null) updatePeriodOptions(Integer.parseInt(newYear));
        });

        // 3. Lắng nghe thay đổi kỳ
        periodmonth.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newPeriod) -> {
            if (newPeriod != null && !isUpdating) {
                refreshDashboard();
            }
        });

        // 4. Mặc định chọn năm hiện tại
        periodyear.setValue(String.valueOf(currentYear));
        
        String currentMonthLabel = "Month " + currentMonth;
        if (periodmonth.getItems().contains(currentMonthLabel)) {
            periodmonth.setValue(currentMonthLabel);
        }
    }

    private void updatePeriodOptions(int selectedYear) {
        isUpdating = true; // Bật cờ bắt đầu cập nhật ComboBox kỳ
        
        String previousSelection = periodmonth.getValue();
        periodmonth.getItems().clear();
        periodmonth.getItems().add("All");

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        
        // Tính toán giới hạn theo thời gian thực
        int maxMonth = (selectedYear < currentYear) ? 12 : now.getMonthValue();
        int maxQuarter = (selectedYear < currentYear) ? 4 : (maxMonth - 1) / 3 + 1;

        for (int q = 1; q <= maxQuarter; q++) periodmonth.getItems().add("Quarter " + q);
        for (int m = 1; m <= maxMonth; m++) periodmonth.getItems().add("Month " + m);

        // Khôi phục lựa chọn hoặc đặt mặc định
        if (periodmonth.getItems().contains(previousSelection)) {
            periodmonth.setValue(previousSelection);
        } else {
            periodmonth.setValue("All"); 
        }

        isUpdating = false; // Tắt cờ
        refreshDashboard();
    }

    private void refreshDashboard() {
        String year = periodyear.getValue();
        String period = periodmonth.getValue();
        if (year == null || period == null) return;

        // Tạo filter theo định dạng ReportDAO yêu cầu
        String filter = period.equals("All") ? "Year " + year : year + " - " + period;
        loadChartData(filter);
    }

    private void loadChartData(String filter) {
        barChartRevenue.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue: " + filter);

        List<Report> data = reportDAO.getRevenueByFilter(filter);
        if (data == null || data.isEmpty()) return;

        for (Report r : data) {
            // Lưu ý: r.getLabel() và r.getValue() từ model Report cũ vẫn hoạt động
            series.getData().add(new XYChart.Data<>(r.getLabel(), r.getValue()));
        }

        barChartRevenue.getData().add(series);
        Platform.runLater(() -> addLabelsToBars(series));
    }

    private void addLabelsToBars(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> entry : series.getData()) {
            Node barNode = entry.getNode();
            if (barNode == null) continue;

            Label label = new Label(String.format("$%.0f", entry.getYValue().doubleValue()));
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 11px;");
            label.setMouseTransparent(true);

           

            // Hàm cập nhật vị trí label
            Runnable posUpdater = () -> {
                javafx.geometry.Bounds b = barNode.getBoundsInParent();
                label.setLayoutX(b.getMinX() + (b.getWidth() / 2) - (label.getLayoutBounds().getWidth() / 2));
                label.setLayoutY(b.getMinY() - 15);
            };

            Platform.runLater(posUpdater);
            barNode.boundsInParentProperty().addListener((obs, oldV, newV) -> posUpdater.run());
        }
    }
}