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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author user
 */
public class LayoutProductController implements Initializable {


    @FXML
    private StackPane viewProduct;
    @FXML
    private ToggleGroup toggleProduct;
    @FXML
    private ToggleGroup toggleProduct1;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadView("/manager/product.fxml");
    }

    private void loadView(String fxmlPath) {
        try {

            viewProduct.getChildren().clear();


            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));


            viewProduct.getChildren().add(node);

        } catch (IOException e) {
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();
 
            viewProduct.getChildren().clear();
            viewProduct.getChildren().add(new Label("Không thể tải trang. Lỗi: " + e.getMessage()));
        }
    }

    @FXML
    private void onProductView(ActionEvent event) {
        loadView("/manager/product.fxml");
    }

    @FXML
    private void onCategory(ActionEvent event) {
        loadView("/manager/category.fxml");
    }

    @FXML
    private void onSize(ActionEvent event) {
        loadView("/manager/size.fxml");
    }

    @FXML
    private void onPromotion(ActionEvent event) {
        loadView("/manager/promotion.fxml");
    }

}
