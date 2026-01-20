package controller.manager.report;

import dao.manager.report.ReportDAO;
import model.manager.report.Report;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;

public class TopsellingController implements Initializable {

    @FXML
    private PieChart pieChartProducts;
    @FXML
    private ComboBox<String> periodyear;
    @FXML
    private ComboBox<String> periodmonth;

    private final ReportDAO reportDAO = new ReportDAO();
    private boolean isUpdating = false; // Cờ chặn refresh vòng lặp

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
    }

    private void setupComboBoxes() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // 1. Nạp danh sách năm
        periodyear.getItems().clear();
        for (int y = 2023; y <= currentYear; y++) {
            periodyear.getItems().add(String.valueOf(y));
        }

        // 2. Listener cho Năm: Cập nhật lại danh sách Kỳ
        periodyear.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newYear) -> {
            if (newYear != null) {
                updatePeriodOptions(Integer.parseInt(newYear));
            }
        });

        // 3. Listener cho Kỳ: Vẽ lại biểu đồ
        periodmonth.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newPeriod) -> {
            if (newPeriod != null && !isUpdating) {
                refreshDashboard();
            }
        });

        // 4. Giá trị mặc định ban đầu
        periodyear.setValue(String.valueOf(currentYear));
        String currentMonthLabel = "Month " + currentMonth;
        if (periodmonth.getItems().contains(currentMonthLabel)) {
            periodmonth.setValue(currentMonthLabel);
        }
    }

    private void updatePeriodOptions(int selectedYear) {
        isUpdating = true; // Bắt đầu cập nhật, tạm dừng refresh biểu đồ

        String currentSelect = periodmonth.getValue();
        periodmonth.getItems().clear();
        periodmonth.getItems().add("All");

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        // Tính toán giới hạn thời gian thực
        int maxMonth = (selectedYear < currentYear) ? 12 : now.getMonthValue();
        int maxQuarter = (selectedYear < currentYear) ? 4 : (maxMonth - 1) / 3 + 1;

        for (int q = 1; q <= maxQuarter; q++) {
            periodmonth.getItems().add("Quarter " + q);
        }
        for (int m = 1; m <= maxMonth; m++) {
            periodmonth.getItems().add("Month " + m);
        }

        // Khôi phục lựa chọn cũ hoặc mặc định là "All"
        if (periodmonth.getItems().contains(currentSelect)) {
            periodmonth.setValue(currentSelect);
        } else {
            periodmonth.setValue("All");
        }

        isUpdating = false; // Kết thúc cập nhật
        refreshDashboard(); // Gọi vẽ lại sau khi ComboBox đã ổn định
    }

    private void refreshDashboard() {
        String year = periodyear.getValue();
        String period = periodmonth.getValue();

        if (year == null || period == null) {
            return;
        }

        // Định dạng filter khớp với logic xử lý chuỗi của ReportDAO
        String filter = period.equals("All") ? "Year " + year : year + " - " + period;
        drawTopProductsPieChart(filter);
    }

    private void drawTopProductsPieChart(String filter) {
        // 1. Xóa dữ liệu cũ để tránh lỗi render đè dữ liệu
        pieChartProducts.getData().clear();

        List<Report> data = reportDAO.getTopSellingProducts(filter);

        if (data == null || data.isEmpty()) {
            pieChartProducts.setTitle("No data available for: " + filter);
            return;
        }

        // 2. Sử dụng Platform.runLater để thực hiện thay đổi UI sau khi lấy data xong
        javafx.application.Platform.runLater(() -> {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            for (Report r : data) {
                // Hiển thị nhãn kèm số lượng để người dùng dễ nhìn
                String label = String.format("%s (%.0f)", r.getLabel(), r.getValue());
                pieData.add(new PieChart.Data(label, r.getValue()));
            }

            pieChartProducts.setData(pieData);
            pieChartProducts.setTitle("Top Selling Products (" + filter + ")");

            // Cấu hình thêm để nhãn không bị dính vào nhau
            pieChartProducts.setLabelLineLength(20);
            pieChartProducts.setLabelsVisible(true);
        });
    }
}
