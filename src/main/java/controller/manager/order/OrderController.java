package controller.manager.order;

import dao.manager.order.OrderDAO;
import dao.manager.employee.EmployeeDAO; // Giả sử bạn có DAO này
import java.io.IOException;
import model.manager.employee.Employee; // Giả sử bạn có Model này
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.manager.order.Order;

public class OrderController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Order> tbOrder;
    @FXML
    private TableColumn<Order, Integer> colOrderID;
    @FXML
    private TableColumn<Order, String> colCustomerID; // Hiển thị tên khách hàng
    @FXML
    private TableColumn<Order, String> colEmployeeID; // Hiển thị tên nhân viên
    @FXML
    private TableColumn<Order, LocalDateTime> colOrderDate;
    @FXML
    private TableColumn<Order, Double> colTotalAmount;
    @FXML
    private TableColumn<Order, String> colPayment;

    @FXML
    private ComboBox<Employee> cbCashier;
    @FXML
    private DatePicker dpFrom;
    @FXML
    private DatePicker dpTo;

    private final OrderDAO orderDAO = new OrderDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadCashiers(); // Đổ dữ liệu vào ComboBox nhân viên
        loadData();

        tbOrder.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Kiểm tra nếu là nhấn đúp chuột
                Order selectedOrder = tbOrder.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    showOrderDetailPopup(selectedOrder);
                }
            }
        });

        // Lắng nghe thay đổi của các bộ lọc để tự động tìm kiếm
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> handleAdvancedSearch());
        cbCashier.valueProperty().addListener((obs, oldVal, newVal) -> handleAdvancedSearch());
        dpFrom.valueProperty().addListener((obs, oldVal, newVal) -> handleAdvancedSearch());
        dpTo.valueProperty().addListener((obs, oldVal, newVal) -> handleAdvancedSearch());
    }

    private void showOrderDetailPopup(Order order) {
        try {
            // Tải file FXML của cửa sổ chi tiết
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/order/orderDetail.fxml"));
            Parent root = loader.load();

            // Lấy controller của cửa sổ chi tiết và truyền dữ liệu sang
            OrderDetailController controller = loader.getController();
            controller.setData(order);

            // Khởi tạo cửa sổ (Stage) mới
            Stage stage = new Stage();
            stage.setTitle("Chi tiết hóa đơn #" + order.getOrderID());
            stage.setScene(new Scene(root));

            // Thiết lập Modality để ngăn tương tác với cửa sổ chính khi đang mở popup
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false); // Không cho phép thay đổi kích thước popup

            stage.showAndWait(); // Hiển thị và đợi người dùng đóng cửa sổ

        } catch (IOException e) {
            System.err.println("Lỗi khi mở cửa sổ chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colOrderID.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        colCustomerID.setCellValueFactory(new PropertyValueFactory<>("customerName")); // Phải trùng với field trong Model Order
        colEmployeeID.setCellValueFactory(new PropertyValueFactory<>("employeeName")); // Phải trùng với field trong Model Order
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDateTime"));
        colTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        // 1. Định dạng Nhãn cho Payment Method
        colPayment.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label statusLabel = new Label(item.toUpperCase());
                    String baseStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; "
                            + "-fx-padding: 3 12 3 12; -fx-background-radius: 15;";

                    if (item.equalsIgnoreCase("Cash")) {
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #1E8449;");
                    } else if (item.equalsIgnoreCase("Credit Card")) {
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #1A5276;");
                    } else if (item.equalsIgnoreCase("E-wallet")) {
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #C71585;");
                    } else {
                        statusLabel.setStyle(baseStyle + "-fx-background-color: #BDC3C7;");
                    }
                    setGraphic(statusLabel);
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // 2. Định dạng Ngày tháng
        colOrderDate.setCellFactory(column -> new TableCell<Order, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatter.format(item));
            }
        });

        // 3. Định dạng Tiền tệ
        colTotalAmount.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%,.2f", item));
                setStyle("-fx-alignment: CENTER; -fx-padding: 0 10 0 0; -fx-font-weight: bold;");
            }
        });

        tbOrder.setItems(orderList);
    }

    private void loadCashiers() {
        // Lấy danh sách nhân viên để lọc
        List<Employee> employees = employeeDAO.getCashiersOnly(); // Bạn cần hàm này trong EmployeeDAO
        cbCashier.setItems(FXCollections.observableArrayList(employees));

        // Hiển thị tên nhân viên trong ComboBox thay vì hiển thị Object
        cbCashier.setConverter(new StringConverter<Employee>() {
            @Override
            public String toString(Employee e) {
                return (e == null) ? "All Cashiers" : e.getFullName();
            }

            @Override
            public Employee fromString(String string) {
                return null;
            }
        });
    }

    private void loadData() {
        orderList.setAll(orderDAO.getAllOrders());
    }

    private void handleAdvancedSearch() {
        String keyword = txtSearch.getText();
        Employee selectedEmp = cbCashier.getValue();
        Integer empID = (selectedEmp != null) ? selectedEmp.getEmployeeID() : null;
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        // Gọi hàm search mới trong OrderDAO (như tôi đã hướng dẫn ở câu trước)
        List<Order> result = orderDAO.searchOrdersAdvanced(keyword, empID, from, to);
        orderList.setAll(result);
    }

    @FXML
    private void onSearch(ActionEvent event) {
        handleAdvancedSearch();
    }

    @FXML
    private void onResetFilter(ActionEvent event) {
        txtSearch.clear();
        cbCashier.setValue(null);
        dpFrom.setValue(null);
        dpTo.setValue(null);
        loadData();
    }
}
