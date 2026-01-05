package controller.manager.employee;

import dao.manager.employee.EmployeeShiftDAO;
import model.manager.employee.EmployeeShift;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
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

public class EmployeeShiftController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<EmployeeShift> tbEmployeeShift;
    @FXML private TableColumn<EmployeeShift, Integer> colID;
    @FXML private TableColumn<EmployeeShift, String> colEmployeeName;
    @FXML private TableColumn<EmployeeShift, String> colShiftName;
    @FXML private TableColumn<EmployeeShift, LocalDate> colWorkDate;
    @FXML private TableColumn<EmployeeShift, LocalTime> colCheckIn;
    @FXML private TableColumn<EmployeeShift, LocalTime> colCheckOut;

    private final EmployeeShiftDAO employeeShiftDAO = new EmployeeShiftDAO();
    private ObservableList<EmployeeShift> allAssignments = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();

        // Tìm kiếm thời gian thực (Real-time search)
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });

        // Double-click để mở form chỉnh sửa (Edit form)
        tbEmployeeShift.setRowFactory(tv -> {
            TableRow<EmployeeShift> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openEditForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("employeeShiftID"));
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colShiftName.setCellValueFactory(new PropertyValueFactory<>("shiftName"));
        colWorkDate.setCellValueFactory(new PropertyValueFactory<>("workDate"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
    }

    private void loadData() {
        allAssignments.setAll(employeeShiftDAO.getAllAssignments());
        tbEmployeeShift.setItems(allAssignments);
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tbEmployeeShift.setItems(allAssignments);
            return;
        }
        String lowerCaseFilter = keyword.toLowerCase();
        ObservableList<EmployeeShift> filteredList = allAssignments.stream()
            .filter(es -> es.getEmployeeName().toLowerCase().contains(lowerCaseFilter) || 
                          es.getShiftName().toLowerCase().contains(lowerCaseFilter))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tbEmployeeShift.setItems(filteredList);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/employee/addEmployeeShift.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Assign New Shift");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbEmployeeShift.getScene().getWindow());
            stage.showAndWait();
            
            loadData(); // Tải lại dữ liệu sau khi thêm
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load assignment form.").show();
        }
    }

    private void openEditForm(EmployeeShift selected) {
        try {
            // Lưu ý: Bạn cần tạo EditEmployeeShift.fxml và Controller tương ứng
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/employee/editEmployeeShift.fxml"));
            Parent root = loader.load();
            
            // Giả sử bạn đặt tên controller là EditEmployeeShiftController
            EditEmployeeShiftController controller = loader.getController();
            controller.initData(selected); 

            Stage stage = new Stage();
            stage.setTitle("Edit Shift Assignment");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbEmployeeShift.getScene().getWindow());
            stage.showAndWait();
            
            loadData(); // Tải lại dữ liệu sau khi sửa/xóa
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load edit assignment form.").show();
        }
    }
}