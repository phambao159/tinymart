package controller.manager.employee;

import dao.manager.employee.ShiftDAO;
import model.manager.employee.Shift;
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
import java.time.LocalTime;

public class ShiftController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<Shift> tbShift;
    @FXML private TableColumn<Shift, Integer> colID;
    @FXML private TableColumn<Shift, String> colName;
    @FXML private TableColumn<Shift, LocalTime> colStart;
    @FXML private TableColumn<Shift, LocalTime> colEnd;

    private ShiftDAO shiftDAO = new ShiftDAO();
    private ObservableList<Shift> allShifts = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadShiftData();

        // Tìm kiếm thời gian thực
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });

        // Double-click để mở Edit Form
        tbShift.setRowFactory(tv -> {
            TableRow<Shift> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openEditForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("shiftID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("shiftName"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    }

    private void loadShiftData() {
        allShifts.setAll(shiftDAO.getAllShifts());
        tbShift.setItems(allShifts);
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tbShift.setItems(allShifts);
            return;
        }
        String lowerCaseFilter = keyword.toLowerCase();
        ObservableList<Shift> filteredList = allShifts.stream()
            .filter(s -> s.getShiftName().toLowerCase().contains(lowerCaseFilter))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tbShift.setItems(filteredList);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/employee/addShift.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Add New Shift");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbShift.getScene().getWindow());
            stage.showAndWait();
            
            loadShiftData(); // Refresh table after adding
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load Add Shift form").show();
        }
    }

    private void openEditForm(Shift selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/employee/editShift.fxml"));
            Parent root = loader.load();
            
            EditShiftController controller = loader.getController();
            controller.initData(selected); // Truyền dữ liệu sang form edit

            Stage stage = new Stage();
            stage.setTitle("Edit Shift");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbShift.getScene().getWindow());
            stage.showAndWait();
            
            loadShiftData(); // Refresh table after editing/deleting
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load Edit Shift form").show();
        }
    }

    @FXML
    private void onSearch(ActionEvent event) {
        filterData(txtSearch.getText());
    }
}