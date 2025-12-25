package controller.manager;

import dao.manager.EmployeeDAO;
import model.manager.Employee;
import java.net.URL;
import java.time.LocalDate;
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

public class EmployeeController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Employee> tbEmployee; // Lưu ý: Tên biến khớp với FXML của bạn (nên đổi thành tbEmployee sau này)
    @FXML
    private TableColumn<Employee, Integer> colID;
    @FXML
    private TableColumn<Employee, String> colName;
    @FXML
    private TableColumn<Employee, LocalDate> colDob;
    @FXML
    private TableColumn<Employee, String> colPhone;
    @FXML
    private TableColumn<Employee, String> colAddress;
    @FXML
    private TableColumn<Employee, String> colRole;
    @FXML
    private TableColumn<Employee, LocalDate> colHireDate;
    @FXML
    private TableColumn<Employee, Long> colSalary;
    @FXML
    private TableColumn<Employee, String> colUser;
    @FXML
    private TableColumn<Employee, String> colPassword;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadEmployeeData();
    }

    private void setupColumns() {
        // Ánh xạ các cột với thuộc tính trong class Employee
        colID.setCellValueFactory(new PropertyValueFactory<>("employeeID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colDob.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
    }

    private void loadEmployeeData() {
        employeeList.clear();
        employeeList.addAll(employeeDAO.getData());
        tbEmployee.setItems(employeeList);
    }

    @FXML
    private void onSearch(ActionEvent event) {

    }

    @FXML
    private void onAdd(ActionEvent event) {
 
    }
}