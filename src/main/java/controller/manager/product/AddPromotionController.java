package controller.manager.product;

import dao.manager.product.PromotionDAO;
import model.manager.product.Promotion;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddPromotionController implements Initializable {

    @FXML
    private TextField txtName, txtType, txtValue;
    @FXML
    private TextArea txtDescription;
    @FXML
    private DatePicker dpStart, dpEnd;
    @FXML
    private ComboBox<String> cbStatus;

    private final PromotionDAO dao = new PromotionDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        cbStatus.getSelectionModel().selectFirst();
        dpStart.setValue(LocalDate.now());
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            if (txtName.getText().isEmpty() || txtType.getText().isEmpty()) {
                showAlert("Error", "Please fill required fields!");
                return;
            }
            Promotion p = new Promotion(0, txtName.getText(), txtDescription.getText(),
                    txtType.getText(), Double.parseDouble(txtValue.getText()),
                    dpStart.getValue(), dpEnd.getValue(), cbStatus.getValue());

            if (dao.insert(p)) {
                close(event);
            }
        } catch (Exception e) {
            showAlert("Error", "Invalid input!");
        }
    }

    @FXML
    private void onCancel(ActionEvent e) {
        close(e);
    }

    private void close(ActionEvent e) {
        ((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
    }

    private void showAlert(String t, String c) {
        new Alert(Alert.AlertType.ERROR, c).showAndWait();
    }
}
