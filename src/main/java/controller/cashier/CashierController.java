package controller.cashier;

import dao.cashier.CashierDAO;
import java.io.IOException;
import model.cashier.CartItem;
import model.cashier.Category;
import model.cashier.Product;
import model.cashier.ProductSizeInfo;
import model.manager.employee.Employee;
import dao.cashier.EmployeeShiftDAO;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import model.cashier.Customer;
import javafx.scene.layout.GridPane;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.cashier.OrderViewModel;
import javafx.beans.property.*;

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
    private TableColumn<CartItem, Double> colPrice;
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

    private List<Product> allProducts = new ArrayList<>();

    private Product selectedProductTemp;
    private ProductSizeInfo selectedSizeTemp;
    private ToggleGroup sizeGroup;
    private int currentEmployeeID;
    private Customer currentCustomer = null;
    @FXML
    private Label lblDiscount;
    @FXML
    private TableView<OrderViewModel> tblHistory;
    @FXML
    private TableColumn<OrderViewModel, Integer> colHistId;
    @FXML
    private TableColumn<OrderViewModel, String> colHistTime;
    @FXML
    private TableColumn<OrderViewModel, String> colHistCashier;
    @FXML
    private TableColumn<OrderViewModel, String> colHistCustomer;
    @FXML
    private TableColumn<OrderViewModel, Double> colHistTotal;
    @FXML
    private TableColumn<OrderViewModel, String> colHistMethod;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnHistory;
    @FXML
    private VBox viewSelling;
    @FXML
    private VBox viewHistory;
    @FXML
    private DatePicker dpHistoryDate;
    @FXML
    private TextField txtSearchHistory;

    @FXML
    private Button btnCheckIn;
    @FXML
    private Button btnCheckOut;
    @FXML
    private Label lblShift;
    @FXML
    private TableView<CartItem> tblOrderDetail;
    @FXML
    private TableColumn<CartItem, String> colDetailItem;
    @FXML
    private TableColumn<CartItem, Integer> colDetailQty;
    @FXML
    private TableColumn<CartItem, Double> colDetailTotal;
    @FXML
    private Button btnPaymentProcess;
    @FXML
    private Button btnRemoveItem;
    @FXML
    private Button btnsearchCustomer;
    @FXML
    private Label lblPointDiscount;
    @FXML
    private Label lblHistorySubTotal;
    @FXML
    private Label lblHistoryDiscount;
    @FXML
    private Label lblHistoryPointDiscount;
    @FXML
    private Label lblHistoryTotal;
    @FXML
    private VBox boxCurrentOrder;
    @FXML
    private VBox boxHistoryInfo;

    private final dao.cashier.EmployeeShiftDAO shiftDAO = new dao.cashier.EmployeeShiftDAO();
    private double currentSessionSales = 0.0;
    private int currentShiftID = 1;
    private boolean isUpdatingHistory = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupHistoryTable();
        setupUI();
        loadCategories();
        loadDataFromDB("");
        setSidebarActive(btnHome);

        dpHistoryDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (!isUpdatingHistory) {
                filterHistory();
            }
        });

        txtSearchHistory.textProperty().addListener((obs, oldText, newText) -> {
            if (!isUpdatingHistory) {
                filterHistory();
            }
        });

        txtSearchHistory.textProperty().addListener((obs, oldText, newText) -> {
            filterHistory();
        });
        txtInputId.textProperty().addListener((obs, oldV, newV) -> loadDataFromDB(newV));

        if (util.User.getSession() != null) {
            Employee emp = util.User.getSession().getEmployee();

            setEmployeeID(emp.getEmployeeID());
            setEmployeeName(emp.getFullName());
            setupShiftStatus();
        } else {
            javafx.application.Platform.runLater(() -> {
                showAlert("Access Denied", "No active session found. Please login again.", Alert.AlertType.ERROR);
                try {
                    main.App.setRoot("ui", "login");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            tblHistory.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadHistoryDetails(newSelection);
                }
            });

            colHistId.prefWidthProperty().bind(tblHistory.widthProperty().multiply(0.1));
            colHistTime.prefWidthProperty().bind(tblHistory.widthProperty().multiply(0.25));
            colHistCashier.prefWidthProperty().bind(tblHistory.widthProperty().multiply(0.2));
            colHistCustomer.prefWidthProperty().bind(tblHistory.widthProperty().multiply(0.45));

            colName.prefWidthProperty().bind(tblCart.widthProperty().multiply(0.30));
            colPrice.prefWidthProperty().bind(tblCart.widthProperty().multiply(0.20));
            colQty.prefWidthProperty().bind(tblCart.widthProperty().multiply(0.25));
            colTotal.prefWidthProperty().bind(tblCart.widthProperty().multiply(0.25));

            String currentShiftName = cashierDAO.getCurrentShiftName(currentEmployeeID);
            lblShift.setText("Shift: " + currentShiftName);
        }
    }

    private void setSidebarActive(Button activeBtn) {
        btnHome.getStyleClass().remove("active");
        btnHistory.getStyleClass().remove("active");
        activeBtn.getStyleClass().add("active");
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
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setCellFactory(tc -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", price));
                }
            }
        });
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellFactory(tc -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", total));
                }
            }
        });
        tblCart.setItems(cartList);
    }

    private void setupUI() {
        int nextId = cashierDAO.getNextOrderId();
        lblOrderId.setText("#" + nextId);
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
            renderProductGrid(allProducts);
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
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getCategoryId() == categoryId)
                .collect(Collectors.toList());
        renderProductGrid(filtered);
    }

    private void loadDataFromDB(String keyword) {
        List<Product> products;

        if (keyword == null || keyword.isEmpty()) {
            products = cashierDAO.getAllProducts();
            this.allProducts = new ArrayList<>(products);
        } else {
            products = cashierDAO.searchProducts(keyword);
        }
        renderProductGrid(products);
    }

    private void renderProductGrid(List<Product> products) {
        productGrid.getChildren().clear();
        for (Product p : products) {
            StackPane stackContainer = new StackPane();

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
                card.getChildren().addAll(imgView, nameLbl, priceLbl, outStockLbl);
            } else {
                card.setOnMouseClicked(e -> openOverlay(p));
                card.getChildren().addAll(imgView, nameLbl, priceLbl);
            }
            stackContainer.getChildren().add(card);
            if (p.isHasPromo()) {
                Label promoBadge = new Label("🔥 PROMO");
                promoBadge.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; -fx-padding: 3px 8px; -fx-background-radius: 0 0 0 10; -fx-font-weight: bold; -fx-font-size: 10px;");
                StackPane.setAlignment(promoBadge, Pos.TOP_RIGHT);
                StackPane.setMargin(promoBadge, new Insets(5, 5, 0, 0));

                stackContainer.getChildren().add(promoBadge);
            }
            productGrid.getChildren().add(stackContainer);
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
        if (!shiftDAO.isCheckedIn(currentEmployeeID, currentShiftID)) {
            showAlert("Action Blocked", "Please Check In to start selling!", Alert.AlertType.WARNING);
            return;
        }

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

                        String infoText = String.format("$%.2f (Stock: %d)", size.getPrice(), size.getStock());

                        if (size.getPromoDescription() != null && !size.getPromoDescription().isEmpty()) {
                            infoText += "\n " + size.getPromoDescription();
                            overlayPrice.setStyle("-fx-text-fill: #e55039; -fx-font-weight: bold; -fx-text-alignment: center;");
                        } else {
                            overlayPrice.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                        }

                        overlayPrice.setText(infoText);

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

            cartList.add(new CartItem(
                    selectedProductTemp.getId(),
                    sizeId,
                    finalName,
                    qty,
                    price,
                    selectedSizeTemp.getPromoType(),
                    selectedSizeTemp.getPromoValue()
            ));
        }
        updateTotals();
        tblCart.refresh();
        overlayPane.setVisible(false);
    }

    private void resetCart() {
        cartList.clear();
        tblCart.refresh();
        updateTotals();
        int nextId = cashierDAO.getNextOrderId();
        lblOrderId.setText("#" + nextId);
        currentCustomer = null;
        txtCustomerPhone.clear();
        boxCustomerInfo.setVisible(false);
        boxCustomerInfo.setManaged(false);
    }

    @FXML
    public void Payment(ActionEvent event) {
        if (!shiftDAO.isCheckedIn(currentEmployeeID, currentShiftID)) {
            showAlert("Action Blocked", "You must Check In before payment.", Alert.AlertType.WARNING);
            return;
        }
        
        if (cartList.isEmpty()) {
            showAlert("Empty Cart", "Please add items before payment.", Alert.AlertType.WARNING);
            return;
        }
        paymentDialogResult payResult = showPaymentDialog();
        if (payResult == null) {
            return;
        }

        double amountDue = Double.parseDouble(lblGrandTotal.getText().replace("$", "").trim());
        if ("Cash".equalsIgnoreCase(payResult.paymentMethod)) {
            TextInputDialog payDialog = new TextInputDialog(String.format("%.2f", amountDue));
            payDialog.setTitle("Cash Payment");
            payDialog.setHeaderText("Total Due: $" + String.format("%.2f", amountDue));
            payDialog.setContentText("Cash Received ($):");

            Optional<String> cashInput = payDialog.showAndWait();

            if (cashInput.isPresent()) {
                try {
                    double cashGiven = Double.parseDouble(cashInput.get());
                    if (cashGiven < amountDue) {
                        showAlert("Insufficient Amount", "Cash received is less than total amount!", Alert.AlertType.ERROR);
                        return;
                    }

                    double change = cashGiven - amountDue;
                    Alert changeAlert = new Alert(Alert.AlertType.INFORMATION);
                    changeAlert.setTitle("Transaction Success");
                    changeAlert.setHeaderText("Change: $" + String.format("%.2f", change));
                    changeAlert.setContentText("Click OK to complete transaction.");
                    changeAlert.showAndWait();

                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid number.", Alert.AlertType.ERROR);
                    return;
                }
            } else {
                return;
            }
        }

        try {
            double subTotal = 0;
            double productDiscount = 0;

            for (CartItem item : cartList) {
                double originalPrice = item.getPrice();
                int qty = item.getQuantity();
                double itemTotal = originalPrice * qty;

                subTotal += itemTotal;

                double itemDiscountAmt = 0;
                String promoType = item.getPromotionType();
                double promoVal = item.getPromotionValue();

                if (promoType != null) {
                    if ("BOGO".equalsIgnoreCase(promoType)) {
                        int freeItems = qty / 2;
                        itemDiscountAmt = freeItems * originalPrice;
                    } else if (promoType.toLowerCase().contains("discount") || promoType.toLowerCase().contains("percentage")) {
                        if (promoVal > 0) {
                            itemDiscountAmt = itemTotal * (promoVal / 100.0);
                        }
                    }
                }
                productDiscount += itemDiscountAmt;
                double finalLineTotal = itemTotal - itemDiscountAmt;
                double sellingPrice = (qty > 0) ? (finalLineTotal / qty) : 0;
                item.setSellingPrice(sellingPrice);
            }

            double pointDiscountMoney = 0;
            int pointsUsed = 0;
            double amountBeforePoint = subTotal - productDiscount;

            if (currentCustomer != null && currentCustomer.getPoints() >= 100 && amountBeforePoint >= 10) {
                int blocksFromPoints = currentCustomer.getPoints() / 100;
                int blocksFromBill = (int) (amountBeforePoint / 10);
                int redeemableBlocks = Math.min(blocksFromPoints, blocksFromBill);
                pointDiscountMoney = redeemableBlocks * 10.0;
                pointsUsed = redeemableBlocks * 100;
            }

            double finalTotal = amountBeforePoint - pointDiscountMoney;
            double totalDiscount = productDiscount + pointDiscountMoney;

            Integer custId = (currentCustomer != null) ? currentCustomer.getId() : null;

            int orderId = cashierDAO.createOrder(currentEmployeeID, custId, finalTotal, totalDiscount, pointDiscountMoney, payResult.paymentMethod, new ArrayList<>(cartList));

            if (orderId != -1) {
                currentSessionSales += finalTotal;

                for (CartItem item : cartList) {
                    try {
                        int pId = Integer.parseInt(item.getProductId());
                        cashierDAO.reduceStock(pId, item.getSizeId(), item.getQuantity());
                    } catch (Exception e) {
                    }
                }

                if (currentCustomer != null) {
                    int pointsEarned = (int) (finalTotal / 10);
                    int oldPoints = currentCustomer.getPoints();
                    int newPointBalance = oldPoints - pointsUsed + pointsEarned;
                    cashierDAO.updateCustomerPoints(currentCustomer.getId(), newPointBalance);
                    currentCustomer.setPoints(newPointBalance);
                }

                String billContent = "";
                if (payResult.isPrintBill) {
                    billContent = generateBillContent(orderId, finalTotal, payResult.paymentMethod, subTotal, productDiscount, pointDiscountMoney);
                }

                isUpdatingHistory = true;
                dpHistoryDate.setValue(null);
                txtSearchHistory.clear();
                isUpdatingHistory = false;
                filterHistory();

                if (!tblHistory.getItems().isEmpty()) {
                    tblHistory.getSelectionModel().selectFirst();
                    tblHistory.scrollTo(0);
                }

                if (payResult.isPrintBill) {
                    showBillAlert(billContent);
                } else {
                    showAlert("Success", "Transaction #" + orderId + " completed!", Alert.AlertType.INFORMATION);
                }

                resetCart();
                setCustomerToBill(null);

            } else {
                showAlert("Error", "Transaction failed. Could not save order.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("System Error", "An unexpected error occurred during processing.", Alert.AlertType.ERROR);
        }
    }

    private static class paymentDialogResult {

        String paymentMethod;
        boolean isPrintBill;

        public paymentDialogResult(String pm, boolean print) {
            this.paymentMethod = pm;
            this.isPrintBill = print;
        }

    }

    private paymentDialogResult showPaymentDialog() {
        Dialog<paymentDialogResult> dialog = new Dialog<>();
        dialog.setTitle("Process Payment");
        dialog.setHeaderText("Confirm Transaction");

        ButtonType payButtonType = new ButtonType("Pay Now", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> cbPayment = new ComboBox<>();
        cbPayment.getItems().addAll("Cash", "Credit Card", "E-Wallet");
        cbPayment.getSelectionModel().selectFirst();

        CheckBox chkPrint = new CheckBox("Print Receipt / Bill");
        chkPrint.setSelected(true);

        grid.add(new Label("Payment Method:"), 0, 0);
        grid.add(cbPayment, 1, 0);
        grid.add(chkPrint, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == payButtonType) {
                return new paymentDialogResult(cbPayment.getValue(), chkPrint.isSelected());
            }
            return null;
        });

        Optional<paymentDialogResult> result = dialog.showAndWait();
        return result.orElse(null);

    }

    private String generateBillContent(int orderId, double finalTotal, String paymentMethod, double subTotal, double productDiscount, double pointDiscount) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================\n");
        sb.append("========== TINYMART ==========\n");
        sb.append("==============================\n");
        sb.append("Order ID: #").append(orderId).append("\n");
        sb.append("Date: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");

        String cashierName = "Unknown";
        if (util.User.getSession() != null && util.User.getSession().getEmployee() != null) {
            cashierName = util.User.getSession().getEmployee().getFullName();
        }
        sb.append("Cashier:        ").append(cashierName).append("\n");

        if (currentCustomer != null) {
            sb.append("Customer:       ").append(currentCustomer.getFullName()).append("\n");
        }
        sb.append("------------------------------\n");
        sb.append(String.format("%-20s %5s %8s\n", "Item", "Qty", "Price"));
        sb.append("------------------------------\n");

        for (CartItem item : cartList) {
            String name = item.getProductName();
            if (name.length() > 18) {
                name = name.substring(0, 18) + "..";
            }
            sb.append(String.format("%-18s %3d %9.2f\n", name, item.getQuantity(), item.getPrice() * item.getQuantity()));
        }

        sb.append("------------------------------\n");
        sb.append(String.format("Subtotal:           $%.2f\n", subTotal));

        if (productDiscount > 0) {
            sb.append(String.format("Promo Discount:    -$%.2f\n", productDiscount));
        }

        if (pointDiscount > 0) {
            sb.append(String.format("Point Discount:    -$%.2f\n", pointDiscount));
        }

        sb.append("------------------------------\n");
        sb.append(String.format("TOTAL:              $%.2f\n", finalTotal));
        sb.append("Payment:            ").append(paymentMethod).append("\n");

        if (currentCustomer != null) {
            sb.append("------------------------------\n");
            sb.append("Points Balance:     ").append(currentCustomer.getPoints()).append(" pts\n");
        }

        sb.append("==============================\n");
        sb.append("      Thank you for shopping!   ");

        return sb.toString();
    }

    private void showBillAlert(String billContent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receipt Printed");
        alert.setHeaderText("Transaction Successful");

        TextArea textArea = new TextArea(billContent);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 13));

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setContent(expContent);

        alert.getDialogPane().setMinWidth(500);
        alert.getDialogPane().setPrefWidth(500);

        alert.getDialogPane().setMinHeight(500);
        alert.getDialogPane().setPrefHeight(600);

        alert.showAndWait();
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
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Start Shift");
        dialog.setHeaderText("Start your shift");
        dialog.setContentText("Enter Start Cash:");

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/cashier/cashier.css").toExternalForm());

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double startCash = Double.parseDouble(result.get());
                if (startCash <= 100) {
                    showAlert("Invalid Amount", "Start Cash must be greater than $100 for change.", Alert.AlertType.WARNING);
                    return;
                }
                int empId = currentEmployeeID;

                if (shiftDAO.checkIn(empId, currentShiftID, startCash)) {
                    showAlert("Success", "Check-in successful!", Alert.AlertType.INFORMATION);

                    btnCheckIn.setDisable(true);
                    btnCheckIn.setText("Checked In");
                    btnCheckOut.setDisable(false);
                    currentSessionSales = 0.0;
                } else {
                    showAlert("Error", "Check-in failed (You might have already checked in).", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void ShiftEnd(ActionEvent event) {
        java.util.Map<String, String> stats = cashierDAO.getShiftStatistics(currentEmployeeID);
        String startTime = stats.get("StartTime");
        String totalOrders = stats.get("TotalOrders");
        String strTotalRevenue = stats.get("TotalRevenue");

        double totalRevenue = 0;
        double cashRevenue = 0;
        double startCash = 0;

        try {
            totalRevenue = Double.parseDouble(stats.get("TotalRevenue"));
            cashRevenue = Double.parseDouble(stats.getOrDefault("CashRevenue", "0"));
            startCash = Double.parseDouble(stats.getOrDefault("StartCash", "0"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        double expectedCash = startCash + cashRevenue;

        Alert reportAlert = new Alert(Alert.AlertType.CONFIRMATION);
        reportAlert.setTitle("End Shift Report");
        reportAlert.setHeaderText("Session Summary");

        String content = "Cashier: " + lblWelcome.getText().replace("Welcome, ", "") + "\n"
                + "------------------------------------------------\n"
                + "Start Time:   " + startTime + "\n"
                + "Total Orders: " + totalOrders + "\n"
                + "Total Sales:  $" + strTotalRevenue + " (All methods)\n"
                + "------------------------------------------------\n"
                + "Start Cash:   $" + String.format("%.2f", startCash) + "\n"
                + "Cash Sales:   $" + String.format("%.2f", cashRevenue) + "\n"
                + "------------------------------------------------\n"
                + "Expected Drawer: $" + String.format("%.2f", expectedCash) + "\n"
                + "------------------------------------------------\n"
                + "Confirm to Close Shift?";

        reportAlert.setContentText(content);
        reportAlert.getDialogPane().setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 16px;");
        reportAlert.getDialogPane().setMinWidth(600);
        reportAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        reportAlert.setResizable(true);
        Optional<ButtonType> reportResult = reportAlert.showAndWait();
        if (reportResult.isPresent() && reportResult.get() == ButtonType.OK) {

            TextInputDialog dialog = new TextInputDialog("0");
            dialog.setTitle("End Shift - Cash Count");
            String hint = String.format("Start ($%.2f) + Cash Sales ($%.2f)", startCash, cashRevenue);
            dialog.setHeaderText("System Estimate: $" + String.format("%.2f", expectedCash) + "\n(" + hint + ")");
            dialog.setContentText("Enter Actual Cash in Drawer:");

            try {
                dialog.getDialogPane().getStylesheets().add(getClass().getResource("/cashier/cashier.css").toExternalForm());
            } catch (Exception e) {
            }

            Optional<String> cashResult = dialog.showAndWait();
            if (cashResult.isPresent()) {
                try {
                    double endCash = Double.parseDouble(cashResult.get());
                    if (shiftDAO.checkOut(currentEmployeeID, currentShiftID, totalRevenue, endCash)) {
                        double variance = endCash - expectedCash;

                        String msg = "Shift closed successfully.\n";
                        if (Math.abs(variance) > 0.01) {
                            msg += String.format("Variance: $%.2f (Mismatch)", variance);
                        } else {
                            msg += "Balance: Perfect Match!";
                        }

                        showAlert("Shift Ended", msg, Alert.AlertType.INFORMATION);
                        logout(event);

                    } else {
                        showAlert("Error", "Check-out failed. Database error.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid number.", Alert.AlertType.ERROR);
                }
            }
        }
    }

    private void updateTotals() {
        double subTotal = 0;
        double totalDiscount = 0;

        for (CartItem item : cartList) {
            double price = item.getPrice();
            int qty = item.getQuantity();
            double itemTotal = price * qty;

            subTotal += itemTotal;

            String promoType = item.getPromotionType();
            double promoVal = item.getPromotionValue();

            if (promoType != null) {
                if ("BOGO".equalsIgnoreCase(promoType)) {
                    int freeItems = qty / 2;
                    totalDiscount += freeItems * price;
                } else if (promoType.toLowerCase().contains("discount") || promoType.toLowerCase().contains("percentage")) {
                    if (promoVal > 0) {
                        totalDiscount += itemTotal * (promoVal / 100.0);
                    }
                }
            }
        }

        double grandTotal = subTotal - totalDiscount;

        lblSubTotal.setText(String.format("$%.2f", subTotal));
        lblDiscount.setText(String.format("$%.2f", totalDiscount));
        lblGrandTotal.setText(String.format("$%.2f", grandTotal));
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

        if (c != null) {
            boxCustomerInfo.setVisible(true);
            boxCustomerInfo.setManaged(true);

            lblCustomerName.setText("Customer Name: " + c.getFullName());
            lblCustomerPoints.setText("Point: " + String.valueOf(c.getPoints()));
            txtCustomerPhone.setText(c.getPhoneNumber());
        } else {
            boxCustomerInfo.setVisible(true);
            boxCustomerInfo.setManaged(true);

            lblCustomerName.setText("Guest");
            lblCustomerPoints.setText("0");
            txtCustomerPhone.setText("");
        }

        calculateTotal();
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

    @FXML
    public void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Sign out?");
        styleDialog(alert);

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (util.User.getSession() != null) {
                util.User.setSession(null);
            }
            try {
                main.App.setRoot("ui", "login");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Could not load Login screen.", Alert.AlertType.ERROR);
            }
        }
    }

    private void setupHistoryTable() {
        colHistId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOrderId()));
        colHistTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderTime()));
        colHistCashier.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCashierName()));
        colHistCustomer.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getCustomerName();
            return new SimpleStringProperty(name == null ? "Guest" : name);
        });

        colHistTotal.setVisible(false);
        colHistMethod.setVisible(true);

        colHistMethod.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getPaymentMethod())
        );

        if (tblOrderDetail != null) {
            tblOrderDetail.setItems(cartList);

            colDetailItem.setCellValueFactory(new PropertyValueFactory<>("productName"));
            colDetailQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

            colDetailTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        }

        tblHistory.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadHistoryDetails(newSelection);
            }
        });
    }

    @FXML
    public void switchToOrder(ActionEvent event) {
        viewHistory.setVisible(false);
        viewHistory.setManaged(false);
        viewSelling.setVisible(true);
        viewSelling.setManaged(true);

        btnPaymentProcess.setVisible(true);
        btnPaymentProcess.setManaged(true);
        btnRemoveItem.setVisible(true);
        btnRemoveItem.setManaged(true);
        btnsearchCustomer.setVisible(true);
        btnsearchCustomer.setManaged(true);

        if (boxCurrentOrder != null) {
            boxCurrentOrder.setVisible(true);
            boxCurrentOrder.setManaged(true);
        }
        if (boxHistoryInfo != null) {
            boxHistoryInfo.setVisible(false);
            boxHistoryInfo.setManaged(false);
        }

        setSidebarActive(btnHome);
        resetCart();
    }

    @FXML
    public void switchToHistory(ActionEvent event) {
        viewSelling.setVisible(false);
        viewSelling.setManaged(false);
        viewHistory.setVisible(true);
        viewHistory.setManaged(true);

        filterHistory();

        btnPaymentProcess.setVisible(false);
        btnPaymentProcess.setManaged(false);
        btnRemoveItem.setVisible(false);
        btnRemoveItem.setManaged(false);
        btnsearchCustomer.setVisible(false);
        btnsearchCustomer.setManaged(false);

        if (boxCurrentOrder != null) {
            boxCurrentOrder.setVisible(false);
            boxCurrentOrder.setManaged(false);
        }
        if (boxHistoryInfo != null) {
            boxHistoryInfo.setVisible(true);
            boxHistoryInfo.setManaged(true);
        }

        setSidebarActive(btnHistory);
        cartList.clear();
        lblOrderId.setText("#Select Order");

        lblHistorySubTotal.setText("$0.00");
        lblHistoryDiscount.setText("$0.00");
        lblHistoryPointDiscount.setText("");
        lblHistoryTotal.setText("$0.00");
    }

    private void filterHistory() {
        LocalDate date = dpHistoryDate.getValue();
        String keyword = txtSearchHistory.getText().trim();

        List<OrderViewModel> list = cashierDAO.searchOrderHistory(date, keyword);
        tblHistory.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void clearHistoryFilter(ActionEvent event) {
        dpHistoryDate.setValue(null);
        txtSearchHistory.clear();
        filterHistory();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert al = new Alert(type);
        al.setTitle(title);
        al.setHeaderText(title.toUpperCase());
        al.setContentText(content);
        styleDialog(al);

        al.showAndWait();
    }

    private void setupShiftStatus() {
        String currentShiftName = cashierDAO.getCurrentShiftName(currentEmployeeID);
        lblShift.setText("Shift: " + currentShiftName);

        int assignedID = cashierDAO.getAssignedShiftID(currentEmployeeID);

        if (assignedID != 0) {
            currentShiftID = assignedID;
        } else {
            int hour = java.time.LocalTime.now().getHour();
            currentShiftID = (hour < 14) ? 1 : 2;
        }

        if (shiftDAO.isCheckedIn(currentEmployeeID, currentShiftID)) {
            btnCheckIn.setDisable(true);
            btnCheckIn.setText("Checked In");
            btnCheckOut.setDisable(false);
        } else {
            btnCheckIn.setDisable(false);
            btnCheckIn.setText("Check In");
            btnCheckOut.setDisable(true);
        }
    }

    private void loadHistoryDetails(OrderViewModel order) {
        if (order == null) {
            return;
        }
        if (lblOrderId != null) {
            lblOrderId.setText("#" + order.getOrderId());
        }

        if (boxCustomerInfo != null) {
            boxCustomerInfo.setVisible(true);
            boxCustomerInfo.setManaged(true);
        }

        String custName = (order.getCustomerName() == null || order.getCustomerName().equalsIgnoreCase("Guest"))
                ? "Guest" : order.getCustomerName();
        lblCustomerName.setText(custName);

        if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
            txtCustomerPhone.setText(order.getCustomerPhone());
            lblCustomerPoints.setText("Points: " + order.getCustomerPoints());
        } else {
            txtCustomerPhone.setText("Guest / No Phone");
            lblCustomerPoints.setText("");
        }

        List<CartItem> details = cashierDAO.getOrderDetails(order.getOrderId());

        cartList.clear();
        cartList.addAll(details);

        double calculatedSubTotal = 0;
        for (CartItem item : details) {
            calculatedSubTotal += item.getSellingPrice() * item.getQuantity();
        }

        double finalTotal = order.getTotalAmount();
        double totalDiscount = order.getTotalDiscount();
        double pointDiscount = order.getPointDiscount();

        double promoDiscount = totalDiscount - pointDiscount;
        if (promoDiscount < 0) {
            promoDiscount = 0;
        }

        lblHistorySubTotal.setText(String.format("$%.2f", calculatedSubTotal));

        if (promoDiscount > 0) {
            lblHistoryDiscount.setText(String.format("-$%.2f", promoDiscount));
        } else {
            lblHistoryDiscount.setText("$0.00");
        }

        if (pointDiscount > 0) {
            lblHistoryPointDiscount.setText(String.format("-$%.2f", pointDiscount));
        } else {
            lblHistoryPointDiscount.setText("-$0.00");
        }

        lblHistoryTotal.setText(String.format("$%.2f", finalTotal));
    }

    private void calculateTotal() {
        double subTotal = 0;
        double productDiscount = 0;

        for (CartItem item : cartList) {
            subTotal += item.getTotal();
            String promoType = item.getPromotionType();
            double promoVal = item.getPromotionValue();
            double price = item.getPrice();
            int qty = item.getQuantity();
            double itemTotal = price * qty;

            if (promoType != null) {
                if ("BOGO".equalsIgnoreCase(promoType)) {
                    int freeItems = qty / 2;
                    productDiscount += freeItems * price;
                } else if (promoType.toLowerCase().contains("discount") || promoType.toLowerCase().contains("percentage")) {
                    if (promoVal > 0) {
                        productDiscount += itemTotal * (promoVal / 100.0);
                    }
                }
            }
        }

        double amountBeforePoint = subTotal - productDiscount;
        double pointDiscount = 0;

        if (currentCustomer != null && currentCustomer.getPoints() >= 100 && amountBeforePoint >= 10) {
            int blocksFromPoints = currentCustomer.getPoints() / 100;

            int blocksFromBill = (int) (amountBeforePoint / 10);
            int redeemableBlocks = Math.min(blocksFromPoints, blocksFromBill);

            pointDiscount = redeemableBlocks * 10.0;
        }

        double finalTotal = amountBeforePoint - pointDiscount;
        if (finalTotal < 0) {
            finalTotal = 0;
        }

        lblSubTotal.setText(String.format("$%.2f", subTotal));
        if (pointDiscount > 0) {
            lblPointDiscount.setText(String.format("-$%.0f", pointDiscount));
        } else {
            lblPointDiscount.setText("-$0");
        }

        lblGrandTotal.setText(String.format("$%.2f", finalTotal));
    }
}
