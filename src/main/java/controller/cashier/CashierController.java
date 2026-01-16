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
        if (!isUpdatingHistory) filterHistory();
    });

    txtSearchHistory.textProperty().addListener((obs, oldText, newText) -> {
        if (!isUpdatingHistory) filterHistory();
    });

        txtSearchHistory.textProperty().addListener((obs, oldText, newText) -> {
            filterHistory();
        });
        txtInputId.textProperty().addListener((obs, oldV, newV) -> loadDataFromDB(newV));

        if (util.User.getSession() != null) {
            Employee emp = util.User.getSession().getEmployee();

            setEmployeeID(emp.getEmployeeID());
            setEmployeeName(emp.getFullName());

        } else {
            setEmployeeID(2);
        }
        setupShiftStatus();
        tblHistory.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadHistoryDetails(newSelection);
            }
        });

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
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
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
        int nextId = cashierDAO.getNextOrderId();
        lblOrderId.setText("#" + nextId);
        currentCustomer = null;
        txtCustomerPhone.clear();
        boxCustomerInfo.setVisible(false);
        boxCustomerInfo.setManaged(false);
    }

    @FXML
    public void Payment(ActionEvent event) {
        if (cartList.isEmpty()) {
            showAlert("Empty Cart", "Please add items before payment.", Alert.AlertType.WARNING);
            return;
        }

        paymentDialogResult result = showPaymentDialog();
        if (result == null) {
            return;
        }

        double subTotal = 0;
        for (CartItem item : cartList) {
            subTotal += item.getTotal();
        }
        double discount = 0;
        double finalTotal = subTotal - discount;
        Integer custId = (currentCustomer != null) ? currentCustomer.getId() : null;
        int orderId = cashierDAO.createOrder(currentEmployeeID, custId, finalTotal, discount, result.paymentMethod, new ArrayList<>(cartList));

        if (orderId != -1) {
            lblOrderId.setText("#" + orderId);
            currentSessionSales += finalTotal;
            for (CartItem item : cartList) {
                try {
                    int pId = Integer.parseInt(item.getProductId());
                    cashierDAO.reduceStock(pId, item.getSizeId(), item.getQuantity());
                } catch (Exception e) {
                }
            }
            if (currentCustomer != null) {
                int points = (int) finalTotal;
                cashierDAO.updateCustomerPoints(currentCustomer.getId(), currentCustomer.getPoints() + points);
            }
            if (result.isPrintBill) {
                printReceipt(orderId, finalTotal, result.paymentMethod, subTotal, discount);
            } else {
                showAlert("Success", "Transaction #" + orderId + " completed!", Alert.AlertType.INFORMATION);
            }
            resetCart();
            isUpdatingHistory = true;
            dpHistoryDate.setValue(null);
            txtSearchHistory.clear();
            isUpdatingHistory = false;
            filterHistory();
            if (!tblHistory.getItems().isEmpty()) {
                tblHistory.getSelectionModel().selectFirst();
                tblHistory.scrollTo(0);
            }

        } else {
            showAlert("Error", "Transaction failed. Could not save order.", Alert.AlertType.ERROR);
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

    private void printReceipt(int orderId, double total, String method, double subTotal, double discount) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receipt Printed");
        alert.setHeaderText("Transaction Successful");

        StringBuilder bill = new StringBuilder();
        bill.append("================================\n");
        bill.append("          TINYMART POS          \n");
        bill.append("================================\n");
        bill.append(String.format("Order ID: #%d\n", orderId));
        bill.append("Date: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n");
        bill.append("Cashier: " + (lblWelcome.getText().replace("Welcome, ", "")) + "\n");
        if (currentCustomer != null) {
            bill.append("Customer: " + currentCustomer.getFullName() + "\n");
        } else {
            bill.append("Customer: Guest\n");
        }
        bill.append("--------------------------------\n");
        bill.append(String.format("%-20s %3s %8s\n", "Item", "Qty", "Price"));
        bill.append("--------------------------------\n");

        for (CartItem item : cartList) {
            String name = item.getProductName();
            if (name.length() > 20) {
                name = name.substring(0, 17) + "...";
            }
            bill.append(String.format("%-20s %3d %8.2f\n", name, item.getQuantity(), item.getTotal()));
        }

        bill.append("--------------------------------\n");
        bill.append(String.format("Subtotal:       %8.2f\n", subTotal));
        bill.append(String.format("Discount:      -%8.2f\n", discount));
        bill.append(String.format("TOTAL:          %8.2f\n", total));
        bill.append("--------------------------------\n");
        bill.append("Payment: " + method + "\n");
        bill.append("================================\n");

        TextArea textArea = new TextArea(bill.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 12));
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setContent(expContent);
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
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("End Shift");
        dialog.setHeaderText("Session Sales: " + String.format("$%.2f", currentSessionSales));
        dialog.setContentText("End Cash:");

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/cashier/cashier.css").toExternalForm());

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double endCash = Double.parseDouble(result.get());
                int empId = currentEmployeeID;

                if (shiftDAO.checkOut(empId, currentShiftID, currentSessionSales, endCash)) {
                    showAlert("Shift Ended", "Shift ended successfully. See you next time!", Alert.AlertType.INFORMATION);
                    logout(event);
                } else {
                    showAlert("Error", "Check-out failed.", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.", Alert.AlertType.ERROR);
            }
        }
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
        colHistMethod.setVisible(false);
        if (tblOrderDetail != null) {
            colDetailItem.setCellValueFactory(new PropertyValueFactory<>("productName"));
            colDetailQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colDetailTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        }
    }

    @FXML
    public void switchToOrder(ActionEvent event) {
        viewHistory.setVisible(false);
        viewHistory.setManaged(false);

        viewSelling.setVisible(true);
        viewSelling.setManaged(true);

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

        setSidebarActive(btnHistory);
        cartList.clear();
        lblOrderId.setText("#Select Order");
        lblGrandTotal.setText("$0.00");
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
        currentShiftID = shiftDAO.getCurrentShiftID();
        lblShift.setText("Shift: " + (currentShiftID == 1 ? "Morning" : "Afternoon"));
        int empId = currentEmployeeID;
        if (empId != 0 && shiftDAO.isCheckedIn(empId, currentShiftID)) {
            btnCheckIn.setDisable(true);
            btnCheckIn.setText("Checked In");
            btnCheckOut.setDisable(false);
        } else {
            btnCheckIn.setDisable(false);
            btnCheckOut.setDisable(true);
        }
    }

    private void loadHistoryDetails(OrderViewModel order) {
    lblOrderId.setText("#" + order.getOrderId());
    boxCustomerInfo.setVisible(true);
    boxCustomerInfo.setManaged(true);
    
    String custName = order.getCustomerName() == null ? "Guest" : order.getCustomerName();
    lblCustomerName.setText("Name: " + custName);
    
    if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
        txtCustomerPhone.setText(order.getCustomerPhone());
        lblCustomerPoints.setText("Points: -");
    } else {
        txtCustomerPhone.setText("Guest / No Phone");
        lblCustomerPoints.setText("");
    }

    List<CartItem> details = cashierDAO.getOrderDetails(order.getOrderId());    
    cartList.clear();
    cartList.addAll(details);
    lblSubTotal.setText(String.format("$%.2f", order.getTotalAmount())); 
    lblGrandTotal.setText(String.format("$%.2f", order.getTotalAmount()));
    lblDiscount.setText("0"); 
    
    tblCart.refresh();
}
}
