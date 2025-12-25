package controller.manager;

import dao.manager.CategoryDAO;
import model.manager.Category;
import java.net.URL;
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
    CategoryDAO categoryDAO = new CategoryDAO(); 


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        setupTableColumns();
        

        loadData();
    }    
    
    /**
     * Thiết lập liên kết dữ liệu cho các cột.
     */
    private void setupTableColumns() {
        colID.setCellValueFactory(new PropertyValueFactory<>("CategoryID")); 
        colName.setCellValueFactory(new PropertyValueFactory<>("Name")); 
        colDes.setCellValueFactory(new PropertyValueFactory<>("Description")); 
        

    }
    
 
    private void loadData() {
        categoryData.clear();
        

        List<Category> dataFromDB = categoryDAO.getData();
        

        categoryData.addAll(dataFromDB);
        

        tbCategory.setItems(categoryData);
    }


    @FXML
    private void onSearch(ActionEvent event) {
        
    }

    @FXML
    private void onAdd(ActionEvent event) {
    }
}