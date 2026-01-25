package controller.manager.employee;

import dao.manager.employee.EmployeeDAO;
import model.manager.employee.Employee;
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

public class EmployeeController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Employee> tbEmployee;

    // Only keeping the visible columns
    @FXML
    private TableColumn<Employee, Integer> colID;
    @FXML
    private TableColumn<Employee, String> colName;
    @FXML
    private TableColumn<Employee, String> colPhone;
    @FXML
    private TableColumn<Employee, String> colRole;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private ObservableList<Employee> allEmployees = FXCollections.observableArrayList();
    @FXML
    private ToggleGroup statusGroup;
    @FXML
    private RadioButton rbActive;
    @FXML
    private RadioButton rbInactive;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadEmployeeData();

        // 1. Filter theo search text (Real-time)
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // 2. Filter theo RadioButton Status (Real-time)
        statusGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Giữ nguyên logic Double-click để mở Edit
        tbEmployee.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openEditForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void applyFilters() {
        String keyword = txtSearch.getText().toLowerCase().trim();

        // Lấy text của RadioButton đang được chọn (Active hoặc Inactive)
        RadioButton selectedRadio = (RadioButton) statusGroup.getSelectedToggle();
        String selectedStatus = (selectedRadio != null) ? selectedRadio.getText() : "";

        ObservableList<Employee> filteredList = allEmployees.stream()
                .filter(e -> {
                    // 1. Kiểm tra từ khóa (Tên hoặc Số điện thoại)
                    boolean matchesSearch = keyword.isEmpty()
                            || e.getFullName().toLowerCase().contains(keyword)
                            || e.getPhoneNumber().contains(keyword);

                    // 2. Kiểm tra trạng thái (So sánh String)
                    // Lưu ý: e.getStatus() phải khớp chính xác với Text của RadioButton (ví dụ: "Active")
                    boolean matchesStatus = e.getStatus() != null
                            && e.getStatus().equalsIgnoreCase(selectedStatus);

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tbEmployee.setItems(filteredList);
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("employeeID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void loadEmployeeData() {
        allEmployees.setAll(employeeDAO.getData());
        tbEmployee.setItems(allEmployees);
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tbEmployee.setItems(allEmployees);
            return;
        }
        String lowerCaseFilter = keyword.toLowerCase();
        ObservableList<Employee> filteredList = allEmployees.stream()
                .filter(e -> e.getFullName().toLowerCase().contains(lowerCaseFilter)
                || e.getPhoneNumber().contains(lowerCaseFilter))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tbEmployee.setItems(filteredList);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/employee/addEmployee.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Employee");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbEmployee.getScene().getWindow());
            stage.showAndWait();
            loadEmployeeData();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load Add form").show();
        }
    }

    private void openEditForm(Employee selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/employee/editEmployee.fxml"));
            Parent root = loader.load();
            EditEmployeeController controller = loader.getController();
            controller.initData(selected); // Passes all data (hidden & visible) to the edit form

            Stage stage = new Stage();
            stage.setTitle("Edit Employee");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbEmployee.getScene().getWindow());
            stage.showAndWait();
            loadEmployeeData();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load Edit form").show();
        }
    }

    private void onSearch(ActionEvent event) {
        filterData(txtSearch.getText());
    }
}
