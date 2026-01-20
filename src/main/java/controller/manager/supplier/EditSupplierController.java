package controller.manager.supplier;

import dao.manager.supplier.SupplierDAO;
import model.manager.supplier.Supplier;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditSupplierController implements Initializable {

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtContactPerson;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtAddress;

    private SupplierDAO supplierDAO;
    private Supplier currentSupplier;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        supplierDAO = new SupplierDAO();
    }

    // Hàm để nhận dữ liệu từ màn hình quản lý truyền sang
    public void initData(Supplier supplier) {
        this.currentSupplier = supplier;
        txtId.setText(String.valueOf(supplier.getSupplierID()));
        txtName.setText(supplier.getName());
        txtContactPerson.setText(supplier.getContactPerson());
        txtPhone.setText(supplier.getPhoneNumber());
        txtAddress.setText(supplier.getAddress());
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        currentSupplier.setName(txtName.getText().trim());
        currentSupplier.setContactPerson(txtContactPerson.getText().trim());
        currentSupplier.setPhoneNumber(txtPhone.getText().trim());
        currentSupplier.setAddress(txtAddress.getText().trim());

        if (currentSupplier.getName().isEmpty() || currentSupplier.getPhoneNumber().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Name and Phone cannot be empty!");
            return;
        }

        if (supplierDAO.updateSupplier(currentSupplier)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier updated successfully!");
            closeStage(event);
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Are you sure you want to delete this supplier?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (supplierDAO.deleteSupplier(currentSupplier.getSupplierID())) {
                closeStage(event);
            }
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        closeStage(event);
    }

    private void closeStage(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}