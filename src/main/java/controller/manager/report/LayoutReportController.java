/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller.manager.report;

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
public class LayoutReportController implements Initializable {

    @FXML
    private ToggleGroup toggleReport;
    @FXML
    private StackPane viewReport;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadView("/manager/report/revenue.fxml");
    }

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
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();

            viewReport.getChildren().clear();
            viewReport.getChildren().add(new Label("Không thể tải trang. Lỗi: " + e.getMessage()));
        }
    }

}
