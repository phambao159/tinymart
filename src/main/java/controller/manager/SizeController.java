/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller.manager;

import dao.manager.PromotionDAO;
import dao.manager.SizeDAO;
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
import model.manager.Promotion;
import model.manager.Size;

/**
 * FXML Controller class
 *
 * @author user
 */
public class SizeController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableColumn<String, Size> colID;
    @FXML
    private TableColumn<String, Size> colType;
    @FXML
    private TableView<Size> tbSize;

    /**
     * Initializes the controller class.
     */
      
    private ObservableList<Size> sizeData = FXCollections.observableArrayList();
    private final SizeDAO sizeDAO = new SizeDAO();
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadData();
    }    

     private void setupTableColumns() {

        colID.setCellValueFactory(new PropertyValueFactory<>("SizeID"));
        colType.setCellValueFactory(new PropertyValueFactory<>("Type"));
        
    }
    

    private void loadData() {
        sizeData.clear();
 
        List<Size> dataFromDB = sizeDAO.getData();
        
        sizeData.addAll(dataFromDB);
        

        tbSize.setItems(sizeData);
    }
    @FXML
    private void onSearch(ActionEvent event) {
    }

    @FXML
    private void onAdd(ActionEvent event) {
    }
    
}
