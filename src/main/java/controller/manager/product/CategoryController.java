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
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
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

    private ObservableList<Category> categoryData = FXCollections.observableArrayList();
    private CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadData();

        // Xử lý Double Click vào dòng trong bảng
        tbCategory.setOnMouseClicked((MouseEvent event) -> {
            // Kiểm tra click 2 lần và dòng được chọn không rỗng
            if (event.getClickCount() == 2 && tbCategory.getSelectionModel().getSelectedItem() != null) {
                openEditForm(tbCategory.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void setupTableColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("categoryID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDes.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadData() {
        categoryData.clear();
        List<Category> dataFromDB = categoryDAO.getData();
        categoryData.addAll(dataFromDB);
        tbCategory.setItems(categoryData);
    }

    @FXML
    private void onSearch(ActionEvent event) {
        String keyword = txtSearch.getText().trim();
        categoryData.clear();

        List<Category> result = keyword.isEmpty() 
                ? categoryDAO.getData() 
                : categoryDAO.searchByName(keyword);

        categoryData.addAll(result);
        tbCategory.setItems(categoryData);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/addCategory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Category");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadData()); // Refresh sau khi đóng
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Nút Edit (nếu bạn vẫn muốn giữ nút bấm trên giao diện)
    @FXML
    private void onEdit(ActionEvent event) {
        Category selected = tbCategory.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openEditForm(selected);
        } else {
            showAlert("Thông báo", "Vui lòng chọn một danh mục để sửa!");
        }
    }


    private void openEditForm(Category selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/EditCategory.fxml"));
            Parent root = loader.load();

            EditCategoryController controller = loader.getController();
            controller.initData(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Category: " + selected.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadData()); // Tự động load lại bảng khi đóng form Edit/Delete
            stage.show();
        } catch (IOException e) {
            System.err.println("Lỗi load EditCategory.fxml: " + e.getMessage());
            e.printStackTrace();
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