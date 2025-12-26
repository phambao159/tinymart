package controller.manager;

import dao.manager.CustomerDAO;
import model.manager.Customer;
import java.net.URL;
import java.time.LocalDate;
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

public class CustomerController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Customer> tbCustomer;
    @FXML
    private TableColumn<Customer, Integer> colID;
    @FXML
    private TableColumn<Customer, String> colName;
    @FXML
    private TableColumn<Customer, String> colPhone;
    @FXML
    private TableColumn<Customer, Integer> colPoint;
    @FXML
    private TableColumn<Customer, String> colEmail; 
    @FXML
    private TableColumn<Customer, LocalDate> colRegisDate;

    private CustomerDAO customerDAO = new CustomerDAO();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();
    }    

    private void setupColumns() {
        // Ánh xạ các cột TableView với các thuộc tính trong class Customer
        colID.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colPoint.setCellValueFactory(new PropertyValueFactory<>("points"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRegisDate.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
    }

    private void loadData() {
        if (tbCustomer != null) {
            customerList.setAll(customerDAO.getAllCustomers());
            tbCustomer.setItems(customerList);
        }
    }

    @FXML
    private void onSearch(ActionEvent event) {

    }

    @FXML
    private void onAdd(ActionEvent event) {
        // Logic mở Dialog/Stage để thêm khách hàng mới
        System.out.println("Navigate to Add Customer View");
    }
}