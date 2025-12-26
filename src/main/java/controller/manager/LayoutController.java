/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller.manager;

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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


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


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadView("/manager/report/layoutReport.fxml");
    }

    @FXML
    private void onDashboard(ActionEvent event) {
        loadView("/manager/report/layoutReport.fxml");
    }

    @FXML
    private void onProduct(ActionEvent event) {
        loadView("/manager/layoutProduct.fxml");
    }

    @FXML
    private void onEmployee(ActionEvent event) {
        loadView("/manager/layoutEmployee.fxml");
    }

    @FXML
    private void onSupplier(ActionEvent event) {
        loadView("/manager/layoutSupplier.fxml");
    }


    @FXML
    private void onCustomer(ActionEvent event) {
        loadView("/manager/customer.fxml");
    }

    @FXML
    private void onLogout(ActionEvent event) {
        
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
}
