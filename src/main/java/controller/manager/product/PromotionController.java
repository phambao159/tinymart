package controller.manager.product;

import dao.manager.product.PromotionDAO;
import model.manager.product.Promotion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.scene.layout.HBox;

public class PromotionController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Promotion> tbPromotion;
    @FXML
    private TableColumn<Promotion, Integer> colID;
    @FXML
    private TableColumn<Promotion, String> colName, colDes, colType, colStatus;
    @FXML
    private TableColumn<Promotion, Double> colValue;
    @FXML
    private TableColumn<Promotion, LocalDate> colStartDate, colEndDate;

    @FXML
    private RadioButton rbActive, rbPending, rbExpired, rbAll;
    @FXML
    private ToggleGroup toggleStatus;

    private final PromotionDAO dao = new PromotionDAO();
    private ObservableList<Promotion> masterData = FXCollections.observableArrayList();
    private FilteredList<Promotion> filteredData;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnApply;
    @FXML
    private HBox filter;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // 1. Setup Columns
        colID.setCellValueFactory(new PropertyValueFactory<>("promotionID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDes.setCellValueFactory(new PropertyValueFactory<>("description"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Tạo Label làm nhãn (Badge)
                    Label statusLabel = new Label(status.toUpperCase());

                    // Style cơ bản: chữ trắng, font đậm, bo góc và đệm xung quanh
                    String baseStyle = "-fx-text-fill: white; "
                            + "-fx-font-weight: bold; "
                            + "-fx-padding: 5 10 5 10; "
                            + "-fx-background-radius: 5; "
                            + "-fx-min-width: 80; "
                            + // Đảm bảo độ rộng nhãn bằng nhau
                            "-fx-alignment: CENTER;";

                    if (status.equalsIgnoreCase("Active")) {
                        // Màu xanh lá (Green)
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #1E8449;");
                    } else if (status.equalsIgnoreCase("Expired")) {
                        // Màu đỏ (Red)
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #E74C3C;");
                    } else if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Inactive")) {
                        // Màu cam hoặc xám cho các trạng thái khác
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #F39C12;");
                    } else {
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #95A5A6;");
                    }

                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });

// Căn giữa cột trong TableView
        colStatus.setStyle("-fx-alignment: CENTER;");

        // 2. Setup ToggleGroup (Nếu FXML chưa có)
        if (toggleStatus == null) {
            toggleStatus = new ToggleGroup();
            rbAll.setToggleGroup(toggleStatus);
            rbActive.setToggleGroup(toggleStatus);
            rbPending.setToggleGroup(toggleStatus);
            rbExpired.setToggleGroup(toggleStatus);
        }
        rbAll.setSelected(true);

        // 3. Load Data ban đầu
        loadData();

        // 4. Double click để sửa
        tbPromotion.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tbPromotion.getSelectionModel().getSelectedItem() != null) {
                showForm("/manager/product/EditPromotion.fxml", "Edit Promotion", tbPromotion.getSelectionModel().getSelectedItem());
            }
        });

        // 5. Lắng nghe ô Search để lọc realtime (Tùy chọn thay vì nhấn Enter)
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter();
        });
    }

    private void loadData() {
        masterData.setAll(dao.getData());
        filteredData = new FilteredList<>(masterData, p -> true);
        tbPromotion.setItems(filteredData);
        applyFilter(); // Đảm bảo giữ đúng trạng thái filter sau khi reload
    }

    // Hàm tổng hợp để lọc cả theo Search và RadioButton
    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        RadioButton selectedRadio = (RadioButton) toggleStatus.getSelectedToggle();
        String statusFilter = (selectedRadio == null || selectedRadio == rbAll) ? "" : selectedRadio.getText();

        filteredData.setPredicate(promotion -> {
            // Lọc theo search name
            boolean matchesSearch = promotion.getName().toLowerCase().contains(searchText);

            // Lọc theo status
            boolean matchesStatus = statusFilter.isEmpty() || promotion.getStatus().equalsIgnoreCase(statusFilter);

            return matchesSearch && matchesStatus;
        });
    }

    @FXML
    private void onSearch(ActionEvent event) {
        applyFilter();
    }

    @FXML
    private void onActive(ActionEvent event) {
        applyFilter();
    }

    @FXML
    private void onPending(ActionEvent event) {
        applyFilter();
    }

    @FXML
    private void onExpired(ActionEvent event) {
        applyFilter();
    }

    @FXML
    private void onAll(ActionEvent event) {
        applyFilter();
    }

    @FXML
    private void onAdd(ActionEvent event) {
        showForm("/manager/product/AddPromotion.fxml", "Add New Promotion", null);
    }

    private void showForm(String path, String title, Promotion p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            if (p != null) {
                EditPromotionController controller = loader.getController();
                controller.initData(p);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadData()); // Load lại data khi đóng cửa sổ con
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the form: " + path);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onApply(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/applyPromotion.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Create New Import Voucher");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tbPromotion.getScene().getWindow());
            stage.showAndWait();

            loadData(); // Refresh lại bảng sau khi đóng cửa sổ thêm mới
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
