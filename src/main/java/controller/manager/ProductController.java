/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller.manager;

import dao.manager.CategoryDAO;
import dao.manager.ProductDAO;
import dao.manager.PromotionDAO;
import dao.manager.SizeDAO;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.manager.Category;
import model.manager.Product;
import model.manager.ProductSummary;
import model.manager.Promotion;
import model.manager.Size;

/**
 * FXML Controller class
 *
 * @author user
 */
public class ProductController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbCategory;
    @FXML
    private ComboBox<String> cbSize;
    @FXML
    private ComboBox<String> cbPromotion;
    @FXML
    private ComboBox<String> cbPaging;
    @FXML
    private FlowPane productContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        loadProducts();
        loadCategorys();
        loadPromotions();
        loadSizes();

    }

    private void loadProducts() {

        productContainer.getChildren().clear();
        ProductDAO pb = new ProductDAO();
        List<ProductSummary> products = pb.getProductSummaries();

        try {
            for (ProductSummary product : products) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/productcard.fxml"));

                VBox productCard = loader.load();

                ProductCardController cardController = loader.getController();

                cardController.setData(product);

                productContainer.getChildren().add(productCard);
            }
        } catch (IOException e) {
            System.err.println("Error loading product card FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCategorys() {
        CategoryDAO cd = new CategoryDAO();
        List<Category> cateList = cd.getData();
        ArrayList<String> cateNameList = new ArrayList<>();
        for (Category cate : cateList) {
            cateNameList.add(cate.getName());
        }
        cbCategory.setItems(FXCollections.observableArrayList(cateNameList));

    }

    private void loadPromotions() {
        PromotionDAO pd = new PromotionDAO();
        List<Promotion> proList = pd.getData();
        ArrayList<String> proNamList = new ArrayList<>();
        for (Promotion cate : proList) {
            proNamList.add(cate.getName());
        }
        cbPromotion.setItems(FXCollections.observableArrayList(proNamList));

    }

    private void loadSizes() {
        SizeDAO pd = new SizeDAO();
        List<Size> sizeList = pd.getData();
        ArrayList<String> sizeNameList = new ArrayList<>();
        for (Size size : sizeList) {
            sizeNameList.add(size.getType());
        }
        cbSize.setItems(FXCollections.observableArrayList(sizeNameList));

    }

    @FXML
    private void onAdd(ActionEvent event) {
    }

}
