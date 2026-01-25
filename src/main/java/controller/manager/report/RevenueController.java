package controller.manager.report;

import dao.manager.report.ReportDAO;
import model.manager.report.Report;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class RevenueController implements Initializable {

    @FXML
    private BarChart<String, Number> barChartRevenue;
    @FXML
    private ComboBox<String> periodyear, periodmonth;
    @FXML
    private ComboBox<String> compareYearA, compareMonthA;
    @FXML
    private ComboBox<String> compareYearB, compareMonthB;
    @FXML
    private Label lblGrowth;

    private final ReportDAO reportDAO = new ReportDAO();
    private boolean isUpdating = false;
    @FXML
    private Label lbRevenueA;
    @FXML
    private Label lbRevenueB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupInitialYears();
        setupListeners();

        // Thiết lập giá trị mặc định ban đầu
        LocalDate now = LocalDate.now();
        periodyear.setValue(String.valueOf(now.getYear()));
        compareYearA.setValue(String.valueOf(now.getYear() - 1));
        compareYearB.setValue(String.valueOf(now.getYear()));
    }

    private void setupInitialYears() {
        LocalDate now = LocalDate.now();
        List<String> years = new ArrayList<>();
        for (int y = 2023; y <= now.getYear(); y++) {
            years.add(String.valueOf(y));
        }
        periodyear.getItems().setAll(years);
        compareYearA.getItems().setAll(years);
        compareYearB.getItems().setAll(years);
    }

    private void setupListeners() {
        // Main Chart
        periodyear.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                updatePeriodOptions(periodmonth, Integer.parseInt(newV), true);
            }
        });
        periodmonth.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !isUpdating) {
                refreshDashboard();
            }
        });

        // Compare A
        compareYearA.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                updatePeriodOptions(compareMonthA, Integer.parseInt(newV), false);
            }
        });
        compareMonthA.valueProperty().addListener((obs, oldV, newV) -> calculateComparison());

        // Compare B
        compareYearB.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                updatePeriodOptions(compareMonthB, Integer.parseInt(newV), false);
            }
        });
        compareMonthB.valueProperty().addListener((obs, oldV, newV) -> calculateComparison());
    }

    /**
     * Cập nhật các lựa chọn Kỳ và TỰ ĐỘNG SET THEO THÁNG HIỆN TẠI
     */
    private void updatePeriodOptions(ComboBox<String> comboBox, int selectedYear, boolean isMainChart) {
        isUpdating = true;
        String previousSelection = comboBox.getValue();
        comboBox.getItems().clear();
        comboBox.getItems().add("All");

        LocalDate now = LocalDate.now();
        int maxMonth = (selectedYear < now.getYear()) ? 12 : now.getMonthValue();
        int maxQuarter = (selectedYear < now.getYear()) ? 4 : (maxMonth - 1) / 3 + 1;

        for (int q = 1; q <= maxQuarter; q++) {
            comboBox.getItems().add("Quarter " + q);
        }
        for (int m = 1; m <= maxMonth; m++) {
            comboBox.getItems().add("Month " + m);
        }

        // LOGIC CHỌN THÁNG HIỆN TẠI
        String currentMonthLabel = "Month " + now.getMonthValue();

        if (comboBox.getItems().contains(previousSelection)) {
            // Nếu giá trị cũ vẫn hợp lệ thì giữ lại
            comboBox.setValue(previousSelection);
        } else if (comboBox.getItems().contains(currentMonthLabel)) {
            // Nếu không, ưu tiên chọn Tháng hiện tại
            comboBox.setValue(currentMonthLabel);
        } else {
            // Trường hợp bất khả kháng mới chọn All
            comboBox.setValue("All");
        }

        isUpdating = false;
        if (isMainChart) {
            refreshDashboard();
        } else {
            calculateComparison();
        }
    }

    private void calculateComparison() {
        if (isUpdating) {
            return;
        }

        String yearA = compareYearA.getValue();
        String periodA = compareMonthA.getValue();
        String yearB = compareYearB.getValue();
        String periodB = compareMonthB.getValue();

        if (yearA == null || periodA == null || yearB == null || periodB == null) {
            return;
        }

        // 1. Chuẩn hóa filter gửi xuống DAO
        String filterA = periodA.equals("All") ? "Year " + yearA : yearA + " - " + periodA;
        String filterB = periodB.equals("All") ? "Year " + yearB : yearB + " - " + periodB;

        // 2. Lấy dữ liệu và tính tổng doanh thu
        double totalA = reportDAO.getRevenueByFilter(filterA).stream().mapToDouble(Report::getValue).sum();
        double totalB = reportDAO.getRevenueByFilter(filterB).stream().mapToDouble(Report::getValue).sum();

        // 3. Đổ dữ liệu vào lbRevenueA và lbRevenueB
        // Định dạng: Revenue Year 2024: 1,000 $ hoặc Revenue 2025 - Month 1: 1,200 $
        lbRevenueA.setText(String.format("Revenue %s: %,.0f $", filterA, totalA));
        lbRevenueB.setText(String.format("Revenue %s: %,.0f $", filterB, totalB));

        // 4. Tính toán các chỉ số tăng trưởng (Growth Rate)
        double diffAmount = totalB - totalA;
        double growth = (totalA > 0) ? (diffAmount / totalA) * 100 : (totalB > 0 ? 100 : 0);

        // 5. Cập nhật nhãn Growth với màu sắc trực quan
        String sign = diffAmount >= 0 ? "+" : "";
        lblGrowth.setText(String.format("%s%.1f%% (%s%,.0f $)", sign, growth, sign, diffAmount));

        if (diffAmount >= 0) {
            lblGrowth.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 24px;");
        } else {
            lblGrowth.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 24px;");
        }
    }

    private void refreshDashboard() {
        String year = periodyear.getValue();
        String period = periodmonth.getValue();
        if (year != null && period != null) {
            String filter = period.equals("All") ? "Year " + year : year + " - " + period;
            loadChartData(filter);
        }
    }

    private void loadChartData(String filter) {
        barChartRevenue.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue: " + filter);

        List<Report> data = reportDAO.getRevenueByFilter(filter);
        if (data == null || data.isEmpty()) {
            return;
        }

        for (Report r : data) {
            series.getData().add(new XYChart.Data<>(r.getLabel(), r.getValue()));
        }

        barChartRevenue.getData().add(series);
        Platform.runLater(() -> addLabelsToBars(series));
    }

    private void addLabelsToBars(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> entry : series.getData()) {
            Node barNode = entry.getNode();
            if (barNode == null) {
                continue;
            }

            Label label = new Label(String.format("%,.0f", entry.getYValue().doubleValue()));
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 10px;");
            label.setMouseTransparent(true);

            javafx.scene.Parent parent = barNode.getParent();
            if (parent instanceof javafx.scene.Group) {
                ((javafx.scene.Group) parent).getChildren().add(label);
            }

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
