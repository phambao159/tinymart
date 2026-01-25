package controller.manager.product;

import dao.manager.product.SizeDAO;
import model.manager.product.Size;
import java.io.IOException;
import java.net.URL;
import java.util.List;
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

public class SizeController implements Initializable {

    // --- FXML Components ---
    @FXML
    private TextField txtSearch;
    @FXML
    private RadioButton rbActive;
    @FXML
    private RadioButton rbInactive;
    @FXML
    private ToggleGroup statusGroup;
    @FXML
    private TableView<Size> tbSize;
    @FXML
    private TableColumn<Size, Integer> colID;
    @FXML
    private TableColumn<Size, String> colType;

    // --- Variables ---
    private final ObservableList<Size> sizeData = FXCollections.observableArrayList();
    private final SizeDAO sizeDAO = new SizeDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();

        // 1. Tìm kiếm thời gian thực khi gõ phím
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterData());

        // 2. Lọc khi thay đổi trạng thái RadioButton (Active/Inactive)
        statusGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> filterData());

        // 3. Double click vào dòng để sửa nhanh
        tbSize.setRowFactory(tv -> {
            TableRow<Size> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openEditForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupTable() {
        // Ánh xạ cột từ Model Size (sizeID, type)
        colID.setCellValueFactory(new PropertyValueFactory<>("sizeID"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        tbSize.setItems(sizeData);
    }

    /**
     * Tải toàn bộ dữ liệu từ DB vào danh sách tạm
     */
    private void loadData() {
        sizeData.clear();
        List<Size> dataFromDB = sizeDAO.getData();
        if (dataFromDB != null) {
            sizeData.addAll(dataFromDB);
        }
        filterData(); // Áp dụng bộ lọc ngay sau khi load
    }

    /**
     * Hàm lọc dữ liệu kết hợp cả TextField và RadioButton
     */
    private void filterData() {
        // 1. Lấy từ khóa tìm kiếm
        String keyword = txtSearch.getText().toLowerCase().trim();

        // 2. Xác định trạng thái từ RadioButton
        // Nếu bạn chỉ có 2 nút Active/Inactive thì lấy trực tiếp
        String statusFilter = rbActive.isSelected() ? "Active" : "Inactive";

        // Nếu sau này bạn có thêm nút "All", hãy dùng:
        // String statusFilter = ((RadioButton) statusGroup.getSelectedToggle()).getText();
        // 3. Lọc danh sách bằng Stream API
        ObservableList<Size> filteredList = sizeData.stream()
                .filter(item -> {
                    // Lọc theo từ khóa (ID hoặc Tên loại Size)
                    boolean matchesSearch = keyword.isEmpty()
                            || String.valueOf(item.getSizeID()).contains(keyword)
                            || item.getType().toLowerCase().contains(keyword);

                    // Lọc theo trạng thái String
                    boolean matchesStatus = item.getStatus().equalsIgnoreCase(statusFilter);

                    // Nếu có nút "All" thì dùng logic này:
                    // boolean matchesStatus = statusFilter.equals("All") || item.getStatus().equalsIgnoreCase(statusFilter);
                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        // 4. Cập nhật lại TableView
        tbSize.setItems(filteredList);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        showForm("/manager/product/addSize.fxml", "Add New Size", null);
    }

    private void openEditForm(Size selected) {
        showForm("/manager/product/EditSize.fxml", "Edit Size: " + selected.getType(), selected);
    }

    /**
     * Hàm dùng chung để mở cửa sổ Add/Edit
     */
    private void showForm(String fxmlPath, String title, Size data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Nếu là Edit, truyền dữ liệu qua Controller của form đó
            if (data != null) {
                // Giả định bạn dùng chung hoặc có controller riêng cho Edit
                Object controller = loader.getController();
                if (controller instanceof EditSizeController) {
                    ((EditSizeController) controller).initData(data);
                }
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL); // Chế độ khóa màn hình chính
            stage.setScene(new Scene(root));

            // Khi đóng cửa sổ con thì load lại bảng dữ liệu
            stage.setOnHiding(e -> loadData());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load form: " + fxmlPath);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
