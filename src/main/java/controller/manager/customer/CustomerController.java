package controller.manager.customer;

import dao.manager.customer.CustomerDAO;
import model.manager.customer.Customer;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
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

public class CustomerController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<Customer> tbCustomer;
    @FXML private TableColumn<Customer, Integer> colID;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, Integer> colPoint;
    @FXML private TableColumn<Customer, String> colEmail; 
    @FXML private TableColumn<Customer, LocalDate> colRegisDate;

    private CustomerDAO customerDAO = new CustomerDAO();
    private ObservableList<Customer> allCustomers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();

        // 1. Real-time search filter
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });

        // 2. Double-click to open Edit Form
        tbCustomer.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openEditForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colPoint.setCellValueFactory(new PropertyValueFactory<>("points"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRegisDate.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
    }

    private void loadData() {
        allCustomers.setAll(customerDAO.getAllCustomers());
        tbCustomer.setItems(allCustomers);
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tbCustomer.setItems(allCustomers);
            return;
        }
        String lowerCaseFilter = keyword.toLowerCase();
        ObservableList<Customer> filteredList = allCustomers.stream()
            .filter(c -> c.getFullName().toLowerCase().contains(lowerCaseFilter) || 
                         c.getPhoneNumber().contains(lowerCaseFilter) ||
                         (c.getEmail() != null && c.getEmail().toLowerCase().contains(lowerCaseFilter)))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tbCustomer.setItems(filteredList);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/customer/addCustomer.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Add New Customer");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbCustomer.getScene().getWindow());
            stage.showAndWait();
            
            loadData(); // Refresh table after adding
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load Add form: " + e.getMessage()).show();
        }
    }

    private void openEditForm(Customer selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/customer/editCustomer.fxml"));
            Parent root = loader.load();
            
            EditCustomerController controller = loader.getController();
            controller.initData(selected); 

            Stage stage = new Stage();
            stage.setTitle("Edit Customer");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbCustomer.getScene().getWindow());
            stage.showAndWait();
            
            loadData(); // Refresh table after editing
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load Edit form: " + e.getMessage()).show();
        }
    }

    @FXML
    private void onSearch(ActionEvent event) {
        filterData(txtSearch.getText());
    }
}