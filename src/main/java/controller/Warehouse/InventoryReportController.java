package controller.Warehouse;

import dao.Warehouse.InventoryReportDAO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.scene.layout.FlowPane;
import model.Warehouse.InventoryReport;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class InventoryReportController implements Initializable {

    // Logs Table
    @FXML private TableView<InventoryReport> logTable;
    @FXML private TableColumn<InventoryReport, Number> colLogNo;
    @FXML private TableColumn<InventoryReport, String> colLogProductName;
    @FXML private TableColumn<InventoryReport, String> colLogUpdateTime;
    @FXML private TableColumn<InventoryReport, String> colLogExpireDate;
    @FXML private TableColumn<InventoryReport, String> colLogActionType;
    @FXML private TableColumn<InventoryReport, Integer> colLogUpdatedQuantity;

    // Alert Cards container
    @FXML private FlowPane alertCardContainer;

    @FXML private ComboBox<String> filterComboBox;

    private InventoryReportDAO reportDAO = new InventoryReportDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[DEBUG] InventoryReportController initialized");

        // Log Table setup
        colLogNo.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(logTable.getItems().indexOf(cell.getValue()) + 1));
        colLogProductName.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getProductName()));
        colLogUpdateTime.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getUpdateTime()));
        colLogExpireDate.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getExpireDate()));
        colLogActionType.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getActionType()));
        colLogUpdatedQuantity.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));

        filterComboBox.getItems().addAll("All", "Import", "Auto-Replenish");
        filterComboBox.setValue("All");
        filterComboBox.setOnAction(e -> {
            System.out.println("[DEBUG] Filter changed to: " + filterComboBox.getValue());
            loadLogs();
        });

        loadAlerts();
        loadLogs();
    }

    /**
     * Hiển thị 2 cảnh báo:
     * 1. Sản phẩm có expireDate cận nhất
     * 2. Sản phẩm có stock thấp nhất
     */
    private void loadAlerts() {
        try {
            System.out.println("[DEBUG] Loading alerts...");
            alertCardContainer.getChildren().clear();

            InventoryReport nearestExpire = reportDAO.getNearestExpireProduct();
            InventoryReport lowestStock = reportDAO.getLowestStockProduct();

            List<InventoryReport> alerts = new ArrayList<>();
            if (nearestExpire != null) {
                System.out.println("[DEBUG] Nearest Expire Product: " + nearestExpire.getProductName() + " | Expire: " + nearestExpire.getExpireDate());
                alerts.add(nearestExpire);
            } else {
                System.out.println("[DEBUG] No nearest expire product found.");
            }

            if (lowestStock != null) {
                System.out.println("[DEBUG] Lowest Stock Product: " + lowestStock.getProductName() + " | Qty: " + lowestStock.getQuantity());
                alerts.add(lowestStock);
            } else {
                System.out.println("[DEBUG] No lowest stock product found.");
            }

            for (InventoryReport report : alerts) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Warehouse/InventoryReportCard.fxml"));
                Node card = loader.load();
                InventoryReportCardController controller = loader.getController();
                controller.setData(report);
                alertCardContainer.getChildren().add(card);
                System.out.println("[DEBUG] Added card for product: " + report.getProductName());
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load alert card FXML: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in loadAlerts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLogs() {
        try {
            System.out.println("[DEBUG] Loading logs with filter: " + filterComboBox.getValue());
            String filter = filterComboBox.getValue();
            List<InventoryReport> logs = reportDAO.getLogs(filter);
            logTable.getItems().setAll(logs);
            System.out.println("[DEBUG] Loaded " + logs.size() + " logs.");
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in loadLogs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onRefresh() {
        System.out.println("[DEBUG] Refresh button clicked");
        loadAlerts();
        loadLogs();
    }

    @FXML
    private void onClose() {
        System.out.println("[DEBUG] Close button clicked");
        ((Stage) logTable.getScene().getWindow()).close();
    }
}