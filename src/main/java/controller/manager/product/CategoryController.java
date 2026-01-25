package controller.manager.product;

import dao.manager.product.CategoryDAO;
import model.manager.product.Category;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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

public class CategoryController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Category> tbCategory;
    @FXML
    private TableColumn<Category, Integer> colID;
    @FXML
    private TableColumn<Category, String> colName;
    @FXML
    private TableColumn<Category, String> colDes;

    @FXML
    private RadioButton rbActive, rbInactive;
    @FXML
    private ToggleGroup statusGroup;

    private final ObservableList<Category> categoryData = FXCollections.observableArrayList();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadData();

        // Lắng nghe thay đổi trên RadioButtons để lọc Active/Inactive
        statusGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            loadData();
        });

        // Xử lý Double Click vào dòng trong bảng
        tbCategory.setRowFactory(tv -> {
            TableRow<Category> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openEditForm(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupTableColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("categoryID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDes.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadData() {
        categoryData.clear();
        String status = rbActive.isSelected() ? "Active" : "Inactive";
        
        // Bạn cần viết thêm hàm getDataByStatus(status) trong CategoryDAO
        List<Category> dataFromDB = categoryDAO.getDataByStatus(status);
        
        categoryData.addAll(dataFromDB);
        tbCategory.setItems(categoryData);
    }

    @FXML
    private void onSearch(ActionEvent event) {
        String keyword = txtSearch.getText().trim();
        String status = rbActive.isSelected() ? "Active" : "Inactive";
        
        categoryData.clear();
        // Bạn nên viết hàm searchByNameAndStatus trong DAO để search chuẩn hơn
        List<Category> result = keyword.isEmpty() 
                ? categoryDAO.getDataByStatus(status) 
                : categoryDAO.searchByNameAndStatus(keyword, status);

        categoryData.addAll(result);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/addCategory.fxml"));
            Parent root = loader.load();
            
            AddCategoryController controller = loader.getController();
            controller.setOnSave(this::loadData); // Callback để refresh bảng

            Stage stage = new Stage();
            stage.setTitle("Add New Category");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditForm(Category selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/EditCategory.fxml"));
            Parent root = loader.load();

            EditCategoryController controller = loader.getController();
            controller.initData(selected);
            controller.setOnSave(this::loadData); // Callback để refresh bảng

            Stage stage = new Stage();
            stage.setTitle("Edit Category: " + selected.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}