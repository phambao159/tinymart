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
import model.cashier.Customer;
import javafx.scene.layout.GridPane;
import java.util.Optional;

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
    private Label lblSubTotal, lblGrandTotal, lblOrderId;

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
    @FXML
    private TextField txtCustomerPhone;
    @FXML
    private VBox boxCustomerInfo;
    @FXML
    private Label lblCustomerName;
    @FXML
    private Label lblCustomerPoints;

    private final ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private final CashierDAO cashierDAO = new CashierDAO();
    private int currentOrderId = 123;

    private List<Product> allProductsMaster = new ArrayList<>();

    private Product selectedProductTemp;
    private ProductSizeInfo selectedSizeTemp;
    private ToggleGroup sizeGroup;
    private int currentEmployeeID;
    private Customer currentCustomer = null;
    @FXML
    private Label lblDiscount;

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

            ImageView imgView = new ImageView();
            imgView.setFitHeight(110);
            imgView.setFitWidth(110);
            imgView.setPreserveRatio(true);
            setProductImage(imgView, p.getImagePath());

            Label nameLbl = new Label(p.getName());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-wrap-text: true; -fx-text-alignment: center;");

            Label priceLbl = new Label(String.format("$%.2f", p.getPrice()));
            priceLbl.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");

            if (p.getTotalStock() <= 0) {
                card.setOpacity(0.5);
                card.setDisable(true);

                Label outStockLbl = new Label("OUT OF STOCK");
                outStockLbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

                card.getChildren().add(outStockLbl);
            } else {
                card.setOnMouseClicked(e -> openOverlay(p));
            }

            card.getChildren().addAll(imgView, nameLbl, priceLbl);

            productGrid.getChildren().add(card);
        }
    }

    private void setProductImage(ImageView view, String dbImageName) {
        String defaultImg = "/image/manager/coca.png";
        String targetImg = (dbImageName == null || dbImageName.isEmpty()) ? defaultImg : dbImageName;
        URL url = getClass().getResource("/image/manager/" + targetImg);

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

        spinnerQty.setDisable(true);
        spinnerQty.getValueFactory().setValue(0);

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

                if (size.getStock() <= 0) {
                    btn.setDisable(true);
                    btn.setText(size.getSizeName() + " (0)");
                }

                btn.setOnAction(e -> {
                    if (btn.isSelected()) {
                        selectedSizeTemp = size;
                        overlayPrice.setText(String.format("$%.2f (Stock: %d)", size.getPrice(), size.getStock()));
                        int maxStock = size.getStock();
                        if (maxStock > 0) {
                            spinnerQty.setDisable(false);
                            SpinnerValueFactory<Integer> valueFactory
                                    = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxStock, 1);
                            spinnerQty.setValueFactory(valueFactory);
                        } else {
                            spinnerQty.setDisable(true);
                            spinnerQty.getValueFactory().setValue(0);
                        }
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

        int sizeId = selectedSizeTemp.getSizeId();

        boolean exists = false;
        for (CartItem item : cartList) {
            if (item.getProductName().equals(finalName)) {
                item.setQuantity(item.getQuantity() + qty);
                exists = true;
                break;
            }
        }

        if (!exists) {
            cartList.add(new CartItem(selectedProductTemp.getId(), sizeId, finalName, qty, price));
        }

        updateTotals();
        tblCart.refresh();
        overlayPane.setVisible(false);
    }
    
    private void resetCart() {
        cartList.clear();
        updateTotals();
        currentOrderId++;
        lblOrderId.setText("#" + currentOrderId);
        currentCustomer = null;
        txtCustomerPhone.clear();
        boxCustomerInfo.setVisible(false);
        boxCustomerInfo.setManaged(false);
    }

    @FXML
    public void Payment(ActionEvent event) {
        if (cartList.isEmpty()) return;

        double totalAmount = 0;
        
        for (CartItem item : cartList) {
            totalAmount += item.getTotal();
                        try {
                int pId = Integer.parseInt(item.getProductId());
                int sId = item.getSizeId();
                int qty = item.getQuantity();
                
                cashierDAO.reduceStock(pId, sId, qty);
                
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        // ---------------------

        if (currentCustomer != null) {
            int pointsEarned = (int) totalAmount;
            int newTotalPoints = currentCustomer.getPoints() + pointsEarned;
            cashierDAO.updateCustomerPoints(currentCustomer.getId(), newTotalPoints);

            showAlert("Payment Success",
                    "Paid: $" + String.format("%.2f", totalAmount) + "\n"
                    + "Points Earned: " + pointsEarned + "\n"
                    + "Inventory Updated.",
                    Alert.AlertType.INFORMATION);
        } else {
            showAlert("Payment Success", "Completed & Inventory updated.", Alert.AlertType.INFORMATION);
        }

        resetCart();
        
        loadDataFromDB(txtInputId.getText());
    }

    @FXML
    public void RemoveItem(ActionEvent event) {
        CartItem selected = tblCart.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartList.remove(selected);
            updateTotals();
        }
    }

    @FXML
    public void CheckIn(ActionEvent event) {
        showAlert("Check-in", "Shift started successfully.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void ShiftEnd(ActionEvent event) {
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

    @FXML
    public void SearchCustomer(ActionEvent event) {
        String phone = txtCustomerPhone.getText().trim();
        if (phone.isEmpty()) {
            return;
        }

        Customer c = cashierDAO.findCustomerByPhone(phone);
        if (c != null) {
            setCustomerToBill(c);
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Customer Not Found");
            alert.setHeaderText("Phone number is not registered yet.");
            alert.setContentText("Do you want to create a new membership account?");

            styleDialog(alert);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                showRegisterDialog(phone);
            }
        }
    }

    private void setCustomerToBill(Customer c) {
        this.currentCustomer = c;
        boxCustomerInfo.setVisible(true);
        boxCustomerInfo.setManaged(true);
        lblCustomerName.setText("Name: " + c.getFullName());
        lblCustomerPoints.setText("Points: " + c.getPoints());
        txtCustomerPhone.setText(c.getPhoneNumber());
    }

    private void showRegisterDialog(String prefillPhone) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Register New Member");
        dialog.setHeaderText("Create TinyMart Membership");
        styleDialog(dialog);

        ButtonType registerBtnType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        TextField txtName = new TextField();
        txtName.setPromptText("Ex: Nguyen Van A");

        TextField txtPhone = new TextField(prefillPhone);
        txtPhone.setEditable(false);

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Ex: email@example.com (Optional)");

        Label lblName = new Label("Full Name (*):");
        lblName.setStyle("-fx-font-weight: bold;");
        Label lblPhone = new Label("Phone Number:");
        lblPhone.setStyle("-fx-font-weight: bold;");
        Label lblEmail = new Label("Email Address:");
        lblEmail.setStyle("-fx-font-weight: bold;");

        grid.add(lblName, 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(lblPhone, 0, 1);
        grid.add(txtPhone, 1, 1);
        grid.add(lblEmail, 0, 2);
        grid.add(txtEmail, 1, 2);

        dialog.getDialogPane().setContent(grid);
        javafx.scene.Node registerBtn = dialog.getDialogPane().lookupButton(registerBtnType);
        registerBtn.setDisable(true);

        txtName.textProperty().addListener((o, oldV, newV)
                -> registerBtn.setDisable(newV.trim().isEmpty())
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerBtnType) {
                return new Customer(txtName.getText(), txtPhone.getText(), txtEmail.getText());
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(newCust -> {
            if (cashierDAO.addCustomer(newCust)) {
                showAlert("Success", "Registration was successful. " + newCust.getFullName() + " to TinyMart!", Alert.AlertType.INFORMATION);
                Customer dbCust = cashierDAO.findCustomerByPhone(newCust.getPhoneNumber());
                setCustomerToBill(dbCust);
            } else {
                showAlert("Error", "Registration Failed!", Alert.AlertType.ERROR);
            }
        });
    }

    private void styleDialog(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();

        pane.getStylesheets().add(getClass().getResource("/cashier/cashier.css").toExternalForm());
        pane.getStyleClass().add("my-dialog");

        for (ButtonType type : pane.getButtonTypes()) {
            javafx.scene.Node node = pane.lookupButton(type);
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (type == ButtonType.OK || type == ButtonType.YES || type == ButtonType.FINISH || type.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    btn.getStyleClass().add("btn-green");
                } else {
                    btn.getStyleClass().add("btn-orange");
                }
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert al = new Alert(type);
        al.setTitle(title);
        al.setHeaderText(title.toUpperCase());
        al.setContentText(content);
        styleDialog(al);

        al.showAndWait();
    }
}
