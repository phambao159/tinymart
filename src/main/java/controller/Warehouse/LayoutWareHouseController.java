package controller.Warehouse;

import dao.manager.notification.NotificationDAO;
import dao.Warehouse.EmployeeShiftDAO;
import model.manager.employee.Employee;
import util.User;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import main.App;

public class LayoutWareHouseController implements Initializable {

    @FXML
    private VBox menu;
    @FXML
    private HBox hBoxImage;
    @FXML
    private ImageView imgLogo;
    @FXML
    private HBox hBoxWelcome;
    @FXML
    private Label lbWelcome;
    @FXML
    private StackPane view;

    // Các ToggleButton trong menu (đã có fx:id trong FXML)
    @FXML
    private ToggleButton btnStorage;
    @FXML
    private ToggleButton btnOverview;
    @FXML
    private ToggleButton lbNoti;
    @FXML
    private ToggleButton btnInventoryReport;
    @FXML
    private ToggleButton btnLogout;

    // KHÔNG dùng @FXML cho ToggleGroup; tạo và gán bằng code
    private ToggleGroup toggleMenu;

    private static LayoutWareHouseController instance;

    private final NotificationDAO nDAO = new NotificationDAO();
    private final EmployeeShiftDAO employeeShiftDAO = new EmployeeShiftDAO();

    public LayoutWareHouseController() {
        instance = this;
    }

    public static LayoutWareHouseController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1) Tạo ToggleGroup và gán cho tất cả nút
        toggleMenu = new ToggleGroup();
        btnStorage.setToggleGroup(toggleMenu);
        btnOverview.setToggleGroup(toggleMenu);
        lbNoti.setToggleGroup(toggleMenu);
        btnInventoryReport.setToggleGroup(toggleMenu);
        btnLogout.setToggleGroup(toggleMenu);

        // 2) Đảm bảo không có nút nào được chọn trước khi gán mặc định
        toggleMenu.selectToggle(null);

        // 3) Chọn mặc định Storage và load view
        selectAndLoad(btnStorage, "/Warehouse/Inventory.fxml");

        // 4) Cập nhật số lượng thông báo chưa đọc
        updateNotificationCount();
    }

    private void updateNotificationCount() {
        try {
            int count = nDAO.countNoti("unread");
            lbNoti.setText(String.format("Notification (%d)", count));
        } catch (Exception e) {
            lbNoti.setText("Notification");
            e.printStackTrace();
        }
    }

    // Hàm tiện ích: chọn nút và load view tương ứng
    private void selectAndLoad(ToggleButton button, String fxmlPath) {
        // Bỏ chọn hiện tại (nếu có), rồi chọn nút mới
        toggleMenu.selectToggle(null);
        button.setSelected(true);
        loadView(fxmlPath);
    }

    @FXML
    private void onStorage(ActionEvent event) {
        selectAndLoad(btnStorage, "/Warehouse/Inventory.fxml");
    }

    @FXML
    private void onOverView(ActionEvent event) {
        selectAndLoad(btnOverview, "/Warehouse/Import.fxml");
    }

    @FXML
    private void onNotifi(ActionEvent event) {
        selectAndLoad(lbNoti, "/Warehouse/Notification.fxml");
    }

    @FXML
    private void onInvenRep(ActionEvent event) {
        selectAndLoad(btnInventoryReport, "/Warehouse/InventoryReport.fxml");
    }

    @FXML
    private void onLogout(ActionEvent event) throws IOException {
        // Chọn nút Logout để phản ánh trạng thái, sau đó chuyển scene
        toggleMenu.selectToggle(null);
        btnLogout.setSelected(true);

        try {
            Employee employee = User.getSession() != null ? User.getSession().getEmployee() : null;
            if (employee != null && employee.getRole().equalsIgnoreCase("warehouse")) {
                LocalDate workDate = LocalDate.now();
                employeeShiftDAO.checkOut(employee, workDate);
                System.out.println("✅ [DEBUG] Warehouse EmployeeID=" + employee.getEmployeeID()
                        + " đã check-out cho WorkDate=" + workDate);
            }
        } catch (Exception e) {
            System.err.println("⚠️ [DEBUG] Lỗi khi ghi check-out: " + e.getMessage());
            e.printStackTrace();
        }

        App.setRoot("ui", "login");
    }

    public void loadView(String fxmlPath) {
        try {
            view.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            if (node instanceof Region) {
                Region region = (Region) node;
                region.prefWidthProperty().bind(view.widthProperty());
                region.prefHeightProperty().bind(view.heightProperty());
            }

            view.getChildren().add(node);
        } catch (IOException e) {
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();
            view.getChildren().clear();
            view.getChildren().add(new Label("Không thể tải trang. Lỗi: " + e.getMessage()));
        }
    }
}
