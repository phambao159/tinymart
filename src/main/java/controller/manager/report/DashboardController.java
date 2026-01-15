package controller.manager.report;

import controller.manager.LayoutController;
import dao.manager.order.OrderDAO;
import dao.manager.report.ReportDAO;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.manager.order.Order;
import model.manager.report.Report;

public class DashboardController implements Initializable {

    @FXML
    private VBox cardRevenue, cardOrder, cardCategory, cardCustomer;
    @FXML
    private Label revenue, subRevenue, order, subOrder, category, subCategory, customer, subCustomer;

    @FXML
    private VBox orderContainer;
    @FXML
    private StackPane viewReport;
    @FXML
    private ToggleGroup toggleReport;

    private final ReportDAO reportDAO = new ReportDAO();
    private final OrderDAO orderDAO = new OrderDAO(); // Khai báo OrderDAO
    @FXML
    private Label lbTitleRevenue;
    @FXML
    private Label lbTitleOrder;
    @FXML
    private Label lbCategory;
    @FXML
    private Label lbCustomer;
    @FXML
    private ScrollPane viewOrder;
    @FXML
    private Button btnSeeMore;

    private LayoutController mainController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadView("/manager/report/revenue.fxml"); // Load view mặc định
        loadCardsData(); // Load các con số tổng quát
        lastOrder();    // Đổ danh sách đơn hàng gần đây vào FlowPane
    }

    // --- PHẦN ĐỔ DỮ LIỆU VÀO VBOX (Thay cho FlowPane) ---
    private void lastOrder() {
        // 1. Dọn dẹp container trước khi đổ dữ liệu mới
        orderContainer.getChildren().clear();

        List<Order> orders = orderDAO.getRecentOrders(4);

        for (Order item : orders) {
            try {
                // 2. Load FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/report/cardOrder.fxml"));

                // Khi dùng VBox với fillWidth="true", ta chỉ cần load dưới dạng Node
                Node orderCard = loader.load();

                // 3. Đổ dữ liệu vào Controller
                CardOrderController controller = loader.getController();
                controller.setData(item);

                // 4. (Tùy chọn) Thêm một chút Margin để đẹp hơn, tránh sát mép thanh cuộn
                VBox.setMargin(orderCard, new javafx.geometry.Insets(0, 10, 0, 0));

                // 5. Thêm vào giao diện - VBox tự động lo phần chiều ngang (Responsive)
                orderContainer.getChildren().add(orderCard);

            } catch (IOException e) {
                System.err.println("Lỗi render thẻ đơn hàng: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --- CÁC HÀM BÁO CÁO TỔNG QUÁT ---
    public void loadCardsData() {
        // Revenue
        double currentRevenue = reportDAO.getTotalRevenueLast30Days();
        double prevRevenue = reportDAO.getRevenuePrevious30Days();
        revenue.setText(String.format("$%.2f", currentRevenue));
        subRevenue.setText(calculateGrowth(currentRevenue, prevRevenue) + " vs last 30 days");

        // Orders
        int todayOrders = reportDAO.getOrdersToday();
        int yesterdayOrders = reportDAO.getOrdersYesterday();
        order.setText(String.valueOf(todayOrders));
        subOrder.setText(calculateGrowth(todayOrders, yesterdayOrders) + " vs Yesterday");

        // Top Category
        String topCat = reportDAO.getTopCategoryThisWeek();
        category.setText(topCat == null ? "N/A" : topCat);
        subCategory.setText("Best seller this week");

        // New Customers
        int currentCust = reportDAO.getNewCustomersThisMonth();
        int prevCust = reportDAO.getNewCustomersLastMonth();
        customer.setText("+" + currentCust);
        subCustomer.setText(calculateGrowth(currentCust, prevCust) + " vs last month");
    }

    private String calculateGrowth(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? "+100%" : "0%";
        }
        double growth = ((current - previous) / previous) * 100;
        return String.format("%s%.1f%%", (growth >= 0 ? "+" : ""), growth);
    }

    // --- ĐIỀU HƯỚNG VIEW ---
    @FXML
    private void onRevenue(ActionEvent event) {
        loadView("/manager/report/revenue.fxml");
    }

    @FXML
    private void onTop10(ActionEvent event) {
        loadView("/manager/report/topselling.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            viewReport.getChildren().clear();
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
            viewReport.getChildren().add(node);
        } catch (IOException e) {
            viewReport.getChildren().setAll(new Label("Lỗi: " + e.getMessage()));
        }
    }

    public void setMainController(LayoutController main) {
        this.mainController = main;
    }

    @FXML
    private void onSeeMore(ActionEvent event) {
        if (mainController != null) {
            mainController.onOrder(null); // Gọi trực tiếp hàm chuyển trang của Main Layout
        }
    }
}
