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
import javafx.scene.layout.Pane;

public class RevenueController implements Initializable {

    @FXML
    private ComboBox<String> period;

    @FXML
    private BarChart<String, Number> barChartRevenue; // Khai báo đúng ID trong FXML

    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBox();
        refreshDashboard("Last 7 days");
    }

    private void setupComboBox() {
        period.getItems().addAll("Last 7 days", "Last Month", "Last Year");
        for (int i = 1; i <= 12; i++) {
            period.getItems().add("Month " + i);
        }

        period.setValue("Last 7 days");
        period.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                refreshDashboard(newV);
            }
        });
    }

    private void refreshDashboard(String filterType) {
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

        // Dùng Platform.runLater để đợi Chart dựng xong các thỏi cột (Nodes)
        Platform.runLater(() -> {
            // Đợi thêm một nhịp nhỏ để chắc chắn Node không null
            addLabelsToBars(series);
        });
    }

    private void addLabelsToBars(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> entry : series.getData()) {
            Node node = entry.getNode();

            if (node != null) {
                Label label = new Label(String.format("$%.0f", entry.getYValue().doubleValue()));
                // Đặt màu đỏ hoặc xanh nổi bật để kiểm tra trước
                label.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 12px;");
                label.setMouseTransparent(true);

                // Thêm vào đúng lớp cha
                javafx.scene.Parent parent = node.getParent();
                if (parent instanceof javafx.scene.Group) {
                    if (!((javafx.scene.Group) parent).getChildren().contains(label)) {
                        ((javafx.scene.Group) parent).getChildren().add(label);
                    }
                } else if (parent instanceof javafx.scene.layout.Pane) {
                    if (!((javafx.scene.layout.Pane) parent).getChildren().contains(label)) {
                        ((javafx.scene.layout.Pane) parent).getChildren().add(label);
                    }
                }

                // Hàm cập nhật vị trí dùng boundsInParent (Chắc chắn hơn layoutX)
                Runnable updatePosition = () -> {
                    javafx.geometry.Bounds bounds = node.getBoundsInParent();
                    // Căn giữa nhãn theo chiều ngang của thỏi cột
                    double x = bounds.getMinX() + (bounds.getWidth() / 2) - (label.getLayoutBounds().getWidth() / 2);
                    // Đặt nhãn cách đỉnh cột 10 pixel
                    double y = bounds.getMinY() - 15;

                    label.setLayoutX(x);
                    label.setLayoutY(y);
                };

                // Chạy ngay lập tức
                Platform.runLater(updatePosition);

                // Lắng nghe thay đổi khi cửa sổ resize hoặc dữ liệu cập nhật
                node.boundsInParentProperty().addListener((obs, oldV, newV) -> updatePosition.run());
            }
        }
    }
}
