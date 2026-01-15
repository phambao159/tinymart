/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller.manager;

import controller.manager.notification.LayoutNotificationController;
import controller.manager.report.DashboardController;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import main.App;

public class LayoutController implements Initializable {

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

    NotificationDAO nDAO = new NotificationDAO();
    @FXML
    private ToggleButton lbOrder;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            updateNotificationBadge();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/report/layout.fxml"));
            Node node = loader.load();

            // Truyền MainLayout cho Dashboard để nó có thể gọi ngược lại
            DashboardController dc = loader.getController();
            dc.setMainController(this);

            view.getChildren().setAll(node);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateNotificationBadge() {
        try {
            int unreadCount = nDAO.countNoti("Unread");

            if (unreadCount > 0) {
                lbNoti.setText("Notification (" + unreadCount + ")");
            } else {
                lbNoti.setText("Notification");
                lbNoti.setStyle("");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void onDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/report/layout.fxml"));
            Node node = loader.load();

            // Truyền MainLayout cho Dashboard để nó có thể gọi ngược lại
            DashboardController dc = loader.getController();
            dc.setMainController(this);

            view.getChildren().setAll(node);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void onProduct(ActionEvent event) {
        loadView("/manager/product/layoutProduct.fxml");
    }

    @FXML
    private void onEmployee(ActionEvent event) {
        loadView("/manager/employee/layoutEmployee.fxml");
    }

    @FXML
    private void onSupplier(ActionEvent event) {
        loadView("/manager/supplier/layoutSupplier.fxml");
    }

    @FXML
    private void onCustomer(ActionEvent event) {
        loadView("/manager/customer/customer.fxml");
    }

    @FXML
    private void onNotification(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/layoutNotification.fxml"));
            Node node = loader.load();

            // Truyền MainLayout cho Dashboard để nó có thể gọi ngược lại
            LayoutNotificationController dc = loader.getController();
            dc.setMainLayoutController(this);

            view.getChildren().setAll(node);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void onLogout(ActionEvent event) throws IOException {
        App.setRoot("ui", "login");

    }

    private void loadView(String fxmlPath) {
        try {

            view.getChildren().clear();

            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));

            view.getChildren().add(node);

        } catch (IOException e) {
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();

            view.getChildren().clear();
            view.getChildren().add(new Label("Không thể tải trang. Lỗi: " + e.getMessage()));
        }
    }

    @FXML
    public void onOrder(ActionEvent event) {
        loadView("/manager/order/order.fxml");
    }

}
