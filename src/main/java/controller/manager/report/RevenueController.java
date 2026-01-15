package controller.manager.report;

import dao.manager.report.ReportDAO;
import model.manager.report.Report;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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
    private ComboBox<String> periodyear; // Kiểu String để dễ xử lý

    @FXML
    private ComboBox<String> periodmonth;

    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
        // Khởi tạo mặc định: Năm hiện tại và "All" (tương ứng với xem cả năm)
        refreshDashboard();
    }

    private void setupComboBoxes() {
        // 1. Lấy thời gian thực từ hệ thống
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // 2. Setup ComboBox Năm (từ 2023 đến hiện tại)
        periodyear.getItems().clear();
        for (int y = 2023; y <= currentYear; y++) {
            periodyear.getItems().add(String.valueOf(y));
        }

        // 3. Lắng nghe sự kiện thay đổi Năm để cập nhật danh sách Tháng/Quý
        periodyear.getSelectionModel().selectedItemProperty().addListener((obs, oldYear, newYearStr) -> {
            if (newYearStr != null) {
                int selectedYear = Integer.parseInt(newYearStr);

                // Lưu lại giá trị đang chọn ở ComboBox Kỳ để tránh bị reset mất dấu
                String currentSelectedPeriod = periodmonth.getValue();

                // Xóa và nạp lại danh sách Kỳ dựa trên Năm được chọn
                periodmonth.getItems().clear();
                periodmonth.getItems().add("All");

                int maxMonth;
                int maxQuarter;

                if (selectedYear < currentYear) {
                    // Nếu là năm cũ: Hiển thị đủ 12 tháng và 4 quý
                    maxMonth = 12;
                    maxQuarter = 4;
                } else {
                    // Nếu là năm hiện tại: Giới hạn theo thời gian thực
                    maxMonth = currentMonth;
                    maxQuarter = (currentMonth - 1) / 3 + 1;
                }

                // Nạp Quý
                for (int q = 1; q <= maxQuarter; q++) {
                    periodmonth.getItems().add("Quarter " + q);
                }
                // Nạp Tháng
                for (int m = 1; m <= maxMonth; m++) {
                    periodmonth.getItems().add("Month " + m);
                }

                // Khôi phục lại giá trị chọn cũ nếu nó vẫn tồn tại trong danh sách mới
                if (periodmonth.getItems().contains(currentSelectedPeriod)) {
                    periodmonth.setValue(currentSelectedPeriod);
                } else {
                    periodmonth.setValue("Month " + (selectedYear < currentYear ? "1" : maxMonth));
                }

                refreshDashboard();
            }
        });

        // 4. Lắng nghe sự kiện thay đổi Kỳ (Tháng/Quý)
        periodmonth.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                refreshDashboard();
            }
        });

        // 5. Thiết lập mặc định ban đầu (Trigger listener ở bước 3)
        periodyear.setValue(String.valueOf(currentYear));
    }

    private void refreshDashboard() {
        String year = periodyear.getValue();
        String period = periodmonth.getValue();

        String filterType;
        if (period.equals("All")) {
            filterType = "Year " + year; // Ví dụ: "Year 2024"
        } else {
            filterType = year + " - " + period; // Ví dụ: "2024 - Month 5" hoặc "2024 - Quarter 1"
        }

        loadChartData(filterType);
    }

    private void loadChartData(String filterType) {
        barChartRevenue.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue: " + filterType);

        List<Report> data = reportDAO.getRevenueByFilter(filterType);
        if (data == null || data.isEmpty()) {
            return;
        }

        for (Report r : data) {
            series.getData().add(new XYChart.Data<>(r.getLabel(), r.getValue()));
        }

        barChartRevenue.getData().add(series);
        Platform.runLater(() -> addLabelsToBars(series));
    }

    // Giữ nguyên hàm addLabelsToBars của bạn...
    private void addLabelsToBars(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> entry : series.getData()) {
            Node node = entry.getNode();
            if (node != null) {
                Label label = new Label(String.format("$%.0f", entry.getYValue().doubleValue()));
                label.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 11px;");
                label.setMouseTransparent(true);

                javafx.scene.Parent parent = node.getParent();
                if (parent instanceof javafx.scene.Group) {
                    ((javafx.scene.Group) parent).getChildren().add(label);
                }

                Runnable updatePosition = () -> {
                    javafx.geometry.Bounds bounds = node.getBoundsInParent();
                    label.setLayoutX(bounds.getMinX() + (bounds.getWidth() / 2) - (label.getLayoutBounds().getWidth() / 2));
                    label.setLayoutY(bounds.getMinY() - 15);
                };
                Platform.runLater(updatePosition);
                node.boundsInParentProperty().addListener((obs, oldV, newV) -> updatePosition.run());
            }
        }
    }
}
