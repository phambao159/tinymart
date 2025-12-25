package controller.manager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class LayoutSupplierController implements Initializable {

    @FXML
    private StackPane viewSupplier;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mặc định khi mở lên sẽ hiện danh sách nhà cung cấp
        loadView("/manager/supplier.fxml"); 
    }

    @FXML
    private void onSupplierList(ActionEvent event) {
        loadView("/manager/supplier.fxml");
    }

    // Hàm dùng chung để tải các file fxml con vào StackPane
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            viewSupplier.getChildren().clear();
            viewSupplier.getChildren().add(root);
        } catch (IOException e) {
            System.err.println("Lỗi khi tải FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}