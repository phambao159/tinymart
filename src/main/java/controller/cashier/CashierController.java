package controller.cashier;

import dao.cashier.CashierDAO;
import model.cashier.CartItem;
import model.cashier.Category;
import model.cashier.Product;
import model.cashier.ProductSizeInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CashierController implements Initializable {

    @FXML
    private TextField txtInputId;
    @FXML
    private TilePane productGrid;
    @FXML
    private HBox categoryContainer;

    @FXML
    private Label lblWelcome;
    @FXML
    private ImageView imgLogo;

    @FXML
    private TableView<CartItem> tblCart;
    @FXML
    private TableColumn<CartItem, String> colName;
    @FXML
    private TableColumn<CartItem, Integer> colQty;
    @FXML
    private TableColumn<CartItem, Double> colTotal;

    @FXML
    private Label lblSubTotal, lblTax, lblGrandTotal, lblOrderId;

    @FXML
    private StackPane overlayPane;
    @FXML
    private ImageView overlayImg;
    @FXML
    private Label overlayName, overlayPrice;
    @FXML
    private FlowPane sizeContainer;
    @FXML
    private Spinner<Integer> spinnerQty;

    private final ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private final CashierDAO cashierDAO = new CashierDAO();
    private int currentOrderId = 123;

    private List<Product> allProductsMaster = new ArrayList<>();

    private Product selectedProductTemp;
    private ProductSizeInfo selectedSizeTemp;
    private ToggleGroup sizeGroup;
    private int currentEmployeeID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupUI();
        loadCategories();
        loadDataFromDB("");

        txtInputId.textProperty().addListener((obs, oldV, newV) -> loadDataFromDB(newV));
        
    }

    public void setEmployeeID(int id) {
        this.currentEmployeeID = id;
        loadEmployeeName();
    }

    private void loadEmployeeName() {
        String fullName = cashierDAO.getEmployeeNameById(this.currentEmployeeID);
        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            String lastName = parts[parts.length - 1];
            lblWelcome.setText("Welcome, " + lastName + "!");
        } else {
            lblWelcome.setText("Welcome, Staff");
        }
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        tblCart.setItems(cartList);
    }

    private void setupUI() {
        lblOrderId.setText("#" + currentOrderId);
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1);
        spinnerQty.setValueFactory(valueFactory);

        try {
            URL logoUrl = getClass().getResource("/ui/logotinymart.png");
            if (logoUrl != null) {
                imgLogo.setImage(new Image(logoUrl.toExternalForm()));
            }
        } catch (Exception e) {
        }
    }

    public void setEmployeeName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            lblWelcome.setText("Welcome, Staff");
            return;
        }
        String[] parts = fullName.trim().split("\\s+");
        String lastName = parts[parts.length - 1];
        lblWelcome.setText("Welcome, " + lastName + "!");
    }

    private void loadCategories() {
        categoryContainer.getChildren().clear();

        Button btnAll = new Button("All Menu");
        btnAll.getStyleClass().add("cat-btn");
        btnAll.getStyleClass().add("active");
        btnAll.setMinWidth(Region.USE_PREF_SIZE);
        btnAll.setOnAction(e -> {
            setActiveCategory(btnAll);
            renderProductGrid(allProductsMaster);
        });
        categoryContainer.getChildren().add(btnAll);

        List<Category> cats = cashierDAO.getAllCategories();
        for (Category cat : cats) {
            Button btn = new Button(cat.getName());
            btn.getStyleClass().add("cat-btn");
            btn.setMinWidth(Region.USE_PREF_SIZE);
            btn.setOnAction(e -> {
                setActiveCategory(btn);
                filterProductsByCategory(cat.getId());
            });
            categoryContainer.getChildren().add(btn);
        }
    }

    private void setActiveCategory(Button activeBtn) {
        categoryContainer.getChildren().forEach(node -> node.getStyleClass().remove("active"));
        activeBtn.getStyleClass().add("active");
    }

    private void filterProductsByCategory(int categoryId) {
        List<Product> filtered = allProductsMaster.stream()
                .filter(p -> p.getCategoryId() == categoryId)
                .collect(Collectors.toList());
        renderProductGrid(filtered);
    }

    private void loadDataFromDB(String keyword) {
        List<Product> products;

        if (keyword == null || keyword.isEmpty()) {
            products = cashierDAO.getAllProducts();
            this.allProductsMaster = new ArrayList<>(products);
        } else {
            products = cashierDAO.searchProducts(keyword);
        }
        renderProductGrid(products);
    }

    private void renderProductGrid(List<Product> products) {
        productGrid.getChildren().clear();
        for (Product p : products) {
            VBox card = new VBox(10);
            card.getStyleClass().add("product-card");
            card.setPrefSize(160, 210);
            card.setAlignment(Pos.CENTER);

            card.setOnMouseClicked(e -> openOverlay(p));

            ImageView imgView = new ImageView();
            imgView.setFitHeight(110);
            imgView.setFitWidth(110);
            imgView.setPreserveRatio(true);
            setProductImage(imgView, p.getImagePath());

            Label nameLbl = new Label(p.getName());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-wrap-text: true; -fx-text-alignment: center;");

            Label priceLbl = new Label(String.format("$%.2f", p.getPrice()));
            priceLbl.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");

            card.getChildren().addAll(imgView, nameLbl, priceLbl);
            productGrid.getChildren().add(card);
        }
    }

    private void setProductImage(ImageView view, String dbImageName) {
        String defaultImg = "/image/coca.png";
        String targetImg = (dbImageName == null || dbImageName.isEmpty()) ? defaultImg : dbImageName;
        URL url = getClass().getResource("/image/" + targetImg);

        if (url == null) {
            url = getClass().getResource(defaultImg);
        }

        if (url != null) {
            view.setImage(new Image(url.toExternalForm()));
        }
    }

    private void openOverlay(Product p) {
        this.selectedProductTemp = p;
        this.selectedSizeTemp = null;

        setProductImage(overlayImg, p.getImagePath());
        overlayName.setText(p.getName());
        overlayPrice.setText("Please select size");
        spinnerQty.getValueFactory().setValue(1);

        sizeContainer.getChildren().clear();
        sizeGroup = new ToggleGroup();
        List<ProductSizeInfo> sizes = cashierDAO.getProductSizes(Integer.parseInt(p.getId()));

        if (sizes.isEmpty()) {
            sizeContainer.getChildren().add(new Label("No sizes available"));
        } else {
            for (ProductSizeInfo size : sizes) {
                ToggleButton btn = new ToggleButton(size.getSizeName());
                btn.getStyleClass().add("size-btn");
                btn.setToggleGroup(sizeGroup);

                btn.setOnAction(e -> {
                    if (btn.isSelected()) {
                        selectedSizeTemp = size;
                        overlayPrice.setText(String.format("$%.2f", size.getPrice()));
                    }
                });
                sizeContainer.getChildren().add(btn);
            }
        }

        overlayPane.setVisible(true);
    }

    @FXML
    public void closeOverlay(ActionEvent event) {
        overlayPane.setVisible(false);
    }

    @FXML
    public void addToCartFromOverlay(ActionEvent event) {
        if (selectedProductTemp == null || selectedSizeTemp == null) {
            showAlert("Warning", "Please select a size!", Alert.AlertType.WARNING);
            return;
        }

        int qty = spinnerQty.getValue();
        double price = selectedSizeTemp.getPrice();
        String finalName = selectedProductTemp.getName() + " (" + selectedSizeTemp.getSizeName() + ")";

        boolean exists = false;
        for (CartItem item : cartList) {
            if (item.getProductName().equals(finalName)) {
                item.setQuantity(item.getQuantity() + qty);
                exists = true;
                break;
            }
        }
        if (!exists) {
            cartList.add(new CartItem(selectedProductTemp.getId(), finalName, qty, price));
        }

        updateTotals();
        tblCart.refresh();
        overlayPane.setVisible(false);
    }

    @FXML
    public void handleCheckout(ActionEvent event) {
        if (cartList.isEmpty()) {
            return;
        }

        currentOrderId++;
        lblOrderId.setText("#" + currentOrderId);

        showAlert("Success", "Payment Successful!", Alert.AlertType.INFORMATION);
        cartList.clear();
        updateTotals();
    }

    @FXML
    public void handleRemoveItem(ActionEvent event) {
        CartItem selected = tblCart.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartList.remove(selected);
            updateTotals();
        }
    }

    @FXML
    public void handleCheckIn(ActionEvent event) {
        showAlert("Check-in", "Shift started successfully.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void handleCheckOut(ActionEvent event) {
        showAlert("Check-out", "Confirm end of shift?", Alert.AlertType.CONFIRMATION);
    }

    private void updateTotals() {
        double sub = 0;
        for (CartItem i : cartList) {
            sub += i.getTotal();
        }

        lblSubTotal.setText(String.format("$%.2f", sub));
        lblGrandTotal.setText(String.format("$%.2f", sub));
    }

    private void showAlert(String t, String c, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(c);
        a.show();
    }
}
