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
    private TextField txtName;
    @FXML
    private TextField txtValue;
    @FXML
    private TextArea txtDescription;
    @FXML
    private DatePicker dpStart, dpEnd;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private Label title;

    private final PromotionDAO dao = new PromotionDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive", "Expired"));
        cbStatus.getSelectionModel().selectFirst();
        dpStart.setValue(LocalDate.now());
        cbType.setItems(FXCollections.observableArrayList("Fixed Discount", "BOGO"));
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            // Kiểm tra các trường bắt buộc (Name và Value)
            if (txtName.getText().isEmpty() || txtValue.getText().isEmpty() || cbType.getValue() == null) {
                showAlert("Error", "Please fill required fields!");
                return;
            }
            
            Promotion p = new Promotion(
                    0, 
                    txtName.getText(), 
                    txtDescription.getText(),
                    cbType.getValue(), 
                    Double.parseDouble(txtValue.getText()),
                    dpStart.getValue(), 
                    dpEnd.getValue(), 
                    cbStatus.getValue()
            );

            if (dao.insert(p)) {
                close(event);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Value must be a valid number!");
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(t);
        alert.setContentText(c);
        alert.showAndWait();
    }
}