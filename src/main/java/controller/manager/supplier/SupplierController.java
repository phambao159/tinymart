package controller.manager.supplier;

import dao.manager.supplier.SupplierDAO;
import model.manager.supplier.Supplier;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SupplierController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<Supplier> tbSupplier;
    @FXML private TableColumn<Supplier, Integer> colID;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colContact;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colAddress;

    private SupplierDAO supplierDAO = new SupplierDAO();
    private ObservableList<Supplier> allSuppliers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadSupplierData();

        // 1. Real-time search filter (Tìm kiếm ngay khi nhập)
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });

        // 2. Double-click to open Edit Form
        tbSupplier.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openEditForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("supplierID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
    }

    private void loadSupplierData() {
        allSuppliers.setAll(supplierDAO.getAllSuppliers());
        tbSupplier.setItems(allSuppliers);
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tbSupplier.setItems(allSuppliers);
            return;
        }
        String lowerCaseFilter = keyword.toLowerCase();
        ObservableList<Supplier> filteredList = allSuppliers.stream()
            .filter(s -> s.getName().toLowerCase().contains(lowerCaseFilter) || 
                         s.getPhoneNumber().contains(lowerCaseFilter) ||
                         s.getContactPerson().toLowerCase().contains(lowerCaseFilter))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tbSupplier.setItems(filteredList);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/addSupplier.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Add New Supplier");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbSupplier.getScene().getWindow());
            stage.showAndWait();
            
            loadSupplierData(); // Refresh data after closing add form
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load Add form").show();
        }
    }

    private void openEditForm(Supplier selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/supplier/editSupplier.fxml"));
            Parent root = loader.load();
            
            EditSupplierController controller = loader.getController();
            controller.initData(selected); // Passes selected object to edit form

            Stage stage = new Stage();
            stage.setTitle("Edit Supplier");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbSupplier.getScene().getWindow());
            stage.showAndWait();
            
            loadSupplierData(); // Refresh data after closing edit form
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load Edit form").show();
        }
    }

    @FXML
    private void onSearch(ActionEvent event) {
        filterData(txtSearch.getText());
    }
}