package controller.Warehouse;

import dao.manager.notification.NotificationDAO;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
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
    @FXML
    private ToggleGroup toggleMenu;
    @FXML
    private ToggleButton lbNoti;

    private static LayoutWareHouseController instance;

    private final NotificationDAO nDAO = new NotificationDAO();

    public LayoutWareHouseController() {
        instance = this;
    }

    public static LayoutWareHouseController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mặc định load Import.fxml
        loadView("/Warehouse/Inventory.fxml");

        // Cập nhật số lượng thông báo chưa đọc
        updateNotificationCount();
    }
//số của Notification

    private void updateNotificationCount() {
        try {
            int count = nDAO.countNoti("unread");
            lbNoti.setText(String.format("Notification (%d)", count));
        } catch (Exception e) {
            lbNoti.setText("Notification");
            e.printStackTrace();
        }
    }

    @FXML
    private void onStorage(ActionEvent event) {
        loadView("/Warehouse/Inventory.fxml");
    }

    @FXML
    private void onOverView(ActionEvent event) {
        loadView("/Warehouse/Import.fxml");
    }

    @FXML
    private void onNotifi(ActionEvent event) {
        openNotificationView();
    }

    @FXML
    private void onLogout(ActionEvent event) throws IOException {
        App.setRoot("ui", "login");
    }

    public void openNotificationView() {
        lbNoti.setSelected(true); // chọn nút Notification
        loadView("/Warehouse/Notification.fxml");
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
