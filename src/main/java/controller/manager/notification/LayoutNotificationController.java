/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller.manager.notification;

import controller.manager.LayoutController;
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

/**
 * FXML Controller class
 *
 * @author user
 */
public class LayoutNotificationController implements Initializable {

    @FXML
    private ToggleGroup toggleNoti;
    @FXML
    private StackPane viewNoti;
    
    /**
     * Initializes the controller class.
     */
    
    public LayoutController mainLayoutController;
    
    public void setMainLayoutController(LayoutController controller) {
        this.mainLayoutController = controller;
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/notification.fxml"));
            Node node = loader.load();

            // Truyền MainLayout cho Dashboard để nó có thể gọi ngược lại
            NotificationController dc = loader.getController();
            dc.setMainLayoutController(mainLayoutController);

            viewNoti.getChildren().setAll(node);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    @FXML
    private void onNoti(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/notification.fxml"));
            Node node = loader.load();

            // Truyền MainLayout cho Dashboard để nó có thể gọi ngược lại
            NotificationController dc = loader.getController();
            dc.setMainLayoutController(mainLayoutController);

            viewNoti.getChildren().setAll(node);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void onSent(ActionEvent event) {
        loadView("/manager/notification/sentNoti.fxml");
    }


    private void loadView(String fxmlPath) {
        try {

            viewNoti.getChildren().clear();

            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));

            viewNoti.getChildren().add(node);

        } catch (IOException e) {
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();

            viewNoti.getChildren().clear();
            viewNoti.getChildren().add(new Label("Không thể tải trang. Lỗi: " + e.getMessage()));
        }
    }


}
