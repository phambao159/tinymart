package controller.manager;

import dao.manager.PromotionDAO; 
import java.net.URL;
import java.time.LocalDate;
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
import model.manager.Promotion; 


/**
 * FXML Controller class cho việc quản lý Promotion.
 */
public class PromotionController implements Initializable {

    @FXML
    private TextField txtSearch;
    
    @FXML
    private TableView<Promotion> tbPromotion;
    @FXML
    private TableColumn<Promotion, Integer> colID;
    @FXML
    private TableColumn<Promotion, String> colName;
    

    @FXML
    private TableColumn<Promotion, String> colDes;
    @FXML
    private TableColumn<Promotion, String> colType;
    @FXML
    private TableColumn<Promotion, Double> colValue;
    
    @FXML
    private TableColumn<Promotion, LocalDate> colStartDate;
    @FXML
    private TableColumn<Promotion, LocalDate> colEndDate;
    @FXML
    private TableColumn<Promotion, String> colStatus;
    
    
    private ObservableList<Promotion> promotionData = FXCollections.observableArrayList();
    private final PromotionDAO promotionDAO = new PromotionDAO();



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadData();
    }    
    

    private void setupTableColumns() {

        colID.setCellValueFactory(new PropertyValueFactory<>("PromotionID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("Name"));
        

        colDes.setCellValueFactory(new PropertyValueFactory<>("Description"));
        colType.setCellValueFactory(new PropertyValueFactory<>("Type"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("Value"));
        

        colStartDate.setCellValueFactory(new PropertyValueFactory<>("StartDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("EndDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("Status"));
    }
    

    private void loadData() {
        promotionData.clear();
 
        List<Promotion> dataFromDB = promotionDAO.getData();
        
        promotionData.addAll(dataFromDB);
        

        tbPromotion.setItems(promotionData);
    }    



    @FXML
    private void onSearch(ActionEvent event) {
 
    }

    @FXML
    private void onAdd(ActionEvent event) {

    }
}