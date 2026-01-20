package controller.manager.report;

import controller.manager.LayoutController;
import dao.manager.report.ReportDAO;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.manager.report.Report;

public class DashboardController implements Initializable {

    @FXML
    private Label revenue;

    // --- Các Label hiển thị con số ---
    @FXML
    private Label subRevenue, order, subOrder, category, subCategory, customer, subCustomer;

    // --- Các Container và UI Control ---
    @FXML
    private StackPane viewReport;

    private final ReportDAO reportDAO = new ReportDAO();
    private LayoutController mainController;
    @FXML
    private VBox cardRevenue;
    @FXML
    private Label lbTitleRevenue;
    @FXML
    private VBox cardOrder;
    @FXML
    private Label lbTitleOrder;
    @FXML
    private VBox cardCategory;
    @FXML
    private Label lbCategory;
    @FXML
    private VBox cardCustomer;
    @FXML
    private Label lbCustomer;
    @FXML
    private ToggleGroup toggleReport;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Load biểu đồ mặc định (Doanh thu) vào vùng dưới
        loadView("/manager/report/revenue.fxml"); 
        
        // 2. Gọi SQL tổng lực để lấy toàn bộ dữ liệu cho 4 thẻ phía trên
        loadCardsData(); 
    }

    /**
     * Lấy toàn bộ dữ liệu Dashboard từ DAO và đổ vào các Label
     */
    public void loadCardsData() {
        Report data = reportDAO.getDashboardData();

        if (data != null) {
            // Đổ dữ liệu Doanh thu (30 ngày qua)
            revenue.setText(String.format("$%.2f", data.getRev30()));
            subRevenue.setText(calculateGrowth(data.getRev30(), data.getRevPrev30()) + " vs last 30 days");

            // Đổ dữ liệu Đơn hàng (Hôm nay)
            order.setText(String.valueOf(data.getOrdersToday()));
            subOrder.setText(calculateGrowth(data.getOrdersToday(), data.getOrdersYesterday()) + " vs Yesterday");

            // Đổ dữ liệu Hạng mục bán chạy (Tuần này)
            category.setText(data.getTopCategory() == null ? "N/A" : data.getTopCategory());
            subCategory.setText("Best seller this week");

            // Đổ dữ liệu Khách hàng mới (Tháng này)
            customer.setText("+" + data.getCustMonth());
            subCustomer.setText(calculateGrowth(data.getCustMonth(), data.getCustPrevMonth()) + " vs last month");
        }
    }

    /**
     * Logic tính % tăng trưởng
     */
    private String calculateGrowth(double current, double previous) {
        if (previous <= 0) {
            return current > 0 ? "+100%" : "0%";
        }
        double growth = ((current - previous) / previous) * 100;
        return String.format("%s%.1f%%", (growth >= 0 ? "+" : ""), growth);
    }

    // --- ĐIỀU HƯỚNG GIỮA CÁC BIỂU ĐỒ ---

    @FXML
    private void onRevenue(ActionEvent event) {
        loadView("/manager/report/revenue.fxml");
    }

    @FXML
    private void onTop10(ActionEvent event) {
        loadView("/manager/report/topselling.fxml");
    }

    /**
     * Hàm dùng chung để load các file FXML con vào vùng StackPane
     */
    private void loadView(String fxmlPath) {
        try {
            viewReport.getChildren().clear();
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
            viewReport.getChildren().add(node);
        } catch (IOException e) {
            viewReport.getChildren().setAll(new Label("Lỗi load view: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * Kết nối với Controller chính để điều hướng trang nếu cần
     */
    public void setMainController(LayoutController main) {
        this.mainController = main;
    }

   
}