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
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author user
 */
public class LayoutEmployeeController implements Initializable {

    @FXML
    private ToggleGroup toggleProduct;
    @FXML
    private StackPane viewEmployee;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadView("/manager/employee.fxml");
    }

    private void loadView(String fxmlPath) {
        try {

            viewEmployee.getChildren().clear();

            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));

            viewEmployee.getChildren().add(node);

        } catch (IOException e) {
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();

            viewEmployee.getChildren().clear();
            viewEmployee.getChildren().add(new Label("Không thể tải trang. Lỗi: " + e.getMessage()));
        }
    }

    @FXML
    private void onEmployee(ActionEvent event) {
        loadView("/manager/employee.fxml");
    }

    @FXML
    private void onShift(ActionEvent event) {
        loadView("/manager/shift.fxml");
    }

    @FXML
    private void onEmployeeShift(ActionEvent event) {
    }

}
