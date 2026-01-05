package controller.manager.product;

import dao.manager.product.PromotionDAO;
import model.manager.product.Promotion;
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
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PromotionController implements Initializable {
    @FXML private TextField txtSearch;
    @FXML private TableView<Promotion> tbPromotion;
    @FXML private TableColumn<Promotion, Integer> colID;
    @FXML private TableColumn<Promotion, String> colName, colDes, colType, colStatus;
    @FXML private TableColumn<Promotion, Double> colValue;
    @FXML private TableColumn<Promotion, LocalDate> colStartDate, colEndDate;

    private final PromotionDAO dao = new PromotionDAO();
    private ObservableList<Promotion> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colID.setCellValueFactory(new PropertyValueFactory<>("promotionID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDes.setCellValueFactory(new PropertyValueFactory<>("description"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        loadData();

        tbPromotion.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tbPromotion.getSelectionModel().getSelectedItem() != null) {
                showForm("/manager/product/EditPromotion.fxml", "Edit Promotion", tbPromotion.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void loadData() {
        data.setAll(dao.getData());
        tbPromotion.setItems(data);
    }

    @FXML
    private void onSearch(ActionEvent event) {
        String key = txtSearch.getText().trim();
        data.setAll(key.isEmpty() ? dao.getData() : dao.searchByName(key));
    }

    @FXML
    private void onAdd(ActionEvent event) {
        showForm("/manager/product/addPromotion.fxml", "Add New Promotion", null);
    }

    private void showForm(String path, String title, Promotion p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            if (p != null) ((EditPromotionController) loader.getController()).initData(p);
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadData());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}