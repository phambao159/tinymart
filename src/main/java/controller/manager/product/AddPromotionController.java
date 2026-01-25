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
        // 1. Reset tất cả style về mặc định trước khi check
        resetStyles();

        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        String type = cbType.getValue();
        String valueStr = txtValue.getText().trim();
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();
        String status = cbStatus.getValue();

        // 2. Kiểm tra từng trường (Not Null & Logic)
        if (name.isEmpty()) {
            showError(txtName, "Promotion name cannot be empty!");
            return;
        }


        if (type == null) {
            showError(cbType, "Please select a promotion type!");
            return;
        }

        // Kiểm tra Value (Phải là số và > 0)
        double value = 0;
        try {
            if (valueStr.isEmpty()) {
                throw new Exception("Value cannot be empty!");
            }
            value = Double.parseDouble(valueStr);
            if (value <= 0) {
                throw new Exception("Value must be greater than 0!");
            }
        } catch (Exception e) {
            showError(txtValue, e instanceof NumberFormatException ? "Value must be a valid number!" : e.getMessage());
            return;
        }

        if (start == null) {
            showError(dpStart, "Please select a start date!");
            return;
        }

        if (end == null) {
            showError(dpEnd, "Please select an end date!");
            return;
        }

        // Kiểm tra logic ngày tháng
        if (end.isBefore(start)) {
            showError(dpEnd, "End date cannot be earlier than start date!");
            return;
        }

        if (status == null) {
            showError(cbStatus, "Please select a status!");
            return;
        }

        // 3. Nếu mọi thứ OK -> Tiến hành lưu
        Promotion p = new Promotion(0, name, description, type, value, start, end, status);

        if (dao.insert(p)) {
            close(event);
        } else {
            showAlert("Error", "Database error. Could not save promotion.");
        }
    }

// Hàm hỗ trợ đổi màu viền đỏ và thông báo lỗi
    private void showError(Control control, String message) {
        control.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5px;");
        control.requestFocus();
        showAlert("Validation Error", message);
    }

// Hàm reset lại style bình thường
    private void resetStyles() {
        txtName.setStyle("");
        txtDescription.setStyle("");
        cbType.setStyle("");
        txtValue.setStyle("");
        dpStart.setStyle("");
        dpEnd.setStyle("");
        cbStatus.setStyle("");
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
