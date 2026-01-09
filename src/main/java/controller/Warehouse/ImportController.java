package controller.Warehouse;

import dao.Warehouse.ImportDAO;
import java.io.IOException;
import model.Warehouse.Imports;
import util.DBConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ImportController implements Initializable {

    @FXML
    private TableView<Imports> tbImport;
    @FXML
    private TableColumn<Imports, Integer> colImportID;
    @FXML
    private TableColumn<Imports, String> colSupplierID;   // đổi sang String để hiển thị tên
    @FXML
    private TableColumn<Imports, String> colEmployeeID;   // đổi sang String để hiển thị tên
    @FXML
    private TableColumn<Imports, LocalDate> colReceiptDate;
    @FXML
    private TableColumn<Imports, Double> colTotalCost;
    @FXML
    private TableColumn<Imports, String> colStatus;
    @FXML
    private TableColumn colIndex;

    @FXML
    private TextField txtImportID;
    @FXML
    private DatePicker dpFromDate;
    @FXML
    private DatePicker dpToDate;
    @FXML
    private TextField txtStatus;

    private ImportDAO importDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colImportID.setCellValueFactory(new PropertyValueFactory<>("importID"));
        colSupplierID.setCellValueFactory(new PropertyValueFactory<>("supplierName")); // hiển thị tên
        colEmployeeID.setCellValueFactory(new PropertyValueFactory<>("employeeName")); // hiển thị tên
        colReceiptDate.setCellValueFactory(new PropertyValueFactory<>("receiptDate"));
        colTotalCost.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cột số thứ tự
        colIndex.setCellFactory(col -> new TableCell<Imports, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        Connection conn = new DBConnection().getConnect();
        importDAO = new ImportDAO(conn);

        resetSearch();
        // Double click row -> mở ImportDetail.fxml
        tbImport.setRowFactory(tv -> {
            TableRow<Imports> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Imports rowData = row.getItem();
                    openImportDetail(rowData);
                }
            });
            return row;
        });
    }

    @FXML
    private void onSearch() {
        String importIdText = txtImportID.getText().trim();
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();
        String statusText = txtStatus.getText().trim();

        List<Imports> allImports = importDAO.getAllImports();
        ObservableList<Imports> filtered = FXCollections.observableArrayList();

        for (Imports imp : allImports) {
            boolean match = true;

            if (!importIdText.isEmpty()) {
                try {
                    int importId = Integer.parseInt(importIdText);
                    if (imp.getImportID() != importId) {
                        match = false;
                    }
                } catch (NumberFormatException e) {
                    match = false;
                }
            }
            if (fromDate != null && imp.getReceiptDate().isBefore(fromDate)) {
                match = false;
            }
            if (toDate != null && imp.getReceiptDate().isAfter(toDate)) {
                match = false;
            }
            if (!statusText.isEmpty() && !imp.getStatus().equalsIgnoreCase(statusText)) {
                match = false;
            }

            if (match) {
                filtered.add(imp);
            }
        }

        tbImport.setItems(filtered);
    }

    @FXML
    private void resetSearch() {
        txtImportID.clear();
        txtStatus.clear();
        dpFromDate.setValue(null);
        dpToDate.setValue(null);

        ObservableList<Imports> data = FXCollections.observableArrayList(importDAO.getAllImports());
        tbImport.setItems(data);
    }

    private void openImportDetail(Imports imp) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Warehouse/ImportDetail.fxml"));
            Scene scene = new Scene(loader.load());

            ImportDetailController detailController = loader.getController();
            detailController.setImportData(imp);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Import Detail");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
