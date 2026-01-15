package controller.manager.report;

import dao.manager.report.ReportDAO;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import model.manager.report.Report;

public class TopsellingController implements Initializable {

    private ReportDAO reportDAO = new ReportDAO();

    @FXML
    private PieChart pieChartProducts;
    @FXML
    private ComboBox<String> periodyear;
    @FXML
    private ComboBox<String> periodmonth;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
        refreshDashboard(); // Gọi lần đầu để hiển thị dữ liệu mặc định
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

        String filter;
        if (period.equals("All")) {
            filter = "Year " + year;
        } else {
            filter = year + " - " + period;
        }

        drawTopProductsPieChart(filter);
    }

    private void drawTopProductsPieChart(String filter) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        // Gọi DAO với tham số filter
        List<Report> data = reportDAO.getTopSellingProducts(filter);

        if (data.isEmpty()) {
            pieChartProducts.setTitle("No data for this period");
        } else {
            pieChartProducts.setTitle("Top Selling Products");
        }

        for (Report r : data) {
            // Hiển thị tên sản phẩm và số lượng bán được trên label
            String labelWithQty = String.format("%s (%.0f)", r.getLabel(), r.getValue());
            pieData.add(new PieChart.Data(labelWithQty, r.getValue()));
        }
        pieChartProducts.setData(pieData);
    }
}
