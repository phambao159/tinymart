package controller.manager;

import dao.manager.SupplierDAO;
import model.manager.Supplier;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class SupplierController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Supplier> tbSupplier;
    @FXML
    private TableColumn<Supplier, Integer> colID;
    @FXML
    private TableColumn<Supplier, String> colName;
    @FXML
    private TableColumn<Supplier, String> colContact;
    @FXML
    private TableColumn<Supplier, String> colPhone;
    @FXML
    private TableColumn<Supplier, String> colAddress;

    private SupplierDAO supplierDAO = new SupplierDAO();
    private ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadSupplierData();
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("supplierID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
    }

    private void loadSupplierData() {
        if (tbSupplier == null) return;
        supplierList.setAll(supplierDAO.getAllSuppliers());
        tbSupplier.setItems(supplierList);
    }

    @FXML
    private void onSearch(ActionEvent event) {
        String keyword = txtSearch.getText().trim();
        List<Supplier> results = supplierDAO.searchSuppliers(keyword);
        supplierList.setAll(results);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        // Logic mở popup thêm nhà cung cấp
        System.out.println("Add supplier clicked");
    }
}