package controller.Warehouse;

import dao.Warehouse.NotificationDAO;
import model.Warehouse.Notification;
import util.DBConnection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationController {

    @FXML
    private TableView<Notification> tbReceived;
    @FXML
    private TableView<Notification> tbSent;

    @FXML
    private TableColumn<Notification, String> colRecvTitle;
    @FXML
    private TableColumn<Notification, String> colRecvContent;
    @FXML
    private TableColumn<Notification, LocalDateTime> colRecvDate;
    @FXML
    private TableColumn<Notification, String> colRecvStatus;

    @FXML
    private TableColumn<Notification, String> colSentTitle;
    @FXML
    private TableColumn<Notification, String> colSentContent;
    @FXML
    private TableColumn<Notification, LocalDateTime> colSentDate;
    @FXML
    private TableColumn<Notification, String> colSentReceiver;

    @FXML
    private DatePicker dpFromDate;
    @FXML
    private DatePicker dpToDate;
    @FXML
    private ComboBox<String> cbFilterStatus;

    @FXML
    private TextField txtTitle;
    @FXML
    private TextArea txtContent;
    @FXML
    private ComboBox<String> cbReceiver;
    @FXML
    private Button btnSend;
    @FXML
    private Button btnFilter;
    @FXML
    private BorderPane root;

    private NotificationDAO dao;
    private final int currentEmployeeID = 3; // giả định nhân viên đang đăng nhập

    private void connectDB() {
        DBConnection db = new DBConnection();
        Connection conn = db.getConnect();
        if (conn != null) {
            this.dao = new NotificationDAO(conn);
            System.out.println("DAO đã được khởi tạo trong Controller!");
        } else {
            System.out.println("Không thể kết nối DB trong Controller.");
        }
    }

    @FXML
    public void initialize() {
        cbReceiver.getItems().addAll("Manager", "Cashier", "All");
        cbFilterStatus.getItems().addAll("Read", "Unread");

        // Cột Received
        colRecvTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colRecvContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colRecvDate.setCellValueFactory(new PropertyValueFactory<>("sentDate"));
        colRecvStatus.setCellValueFactory(cell
                -> new SimpleStringProperty(cell.getValue().isRead() ? "Read" : "Unread"));

        // Cột Sent
        colSentTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSentContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colSentDate.setCellValueFactory(new PropertyValueFactory<>("sentDate"));
        colSentReceiver.setCellValueFactory(new PropertyValueFactory<>("receiverName"));

        // Format ngày giờ
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colRecvDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(fmt));
            }
        });
        colSentDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(fmt));
            }
        });

        // Double-click Received
        tbReceived.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Notification selected = tbReceived.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showNotificationCard(selected, true);
                }
            }
        });

        // Double-click Sent
        tbSent.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Notification selected = tbSent.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showNotificationCard(selected, false);
                }
            }
        });

        connectDB();
        onFilter(); // load dữ liệu ban đầu
    }

    @FXML
    private void onFilter() {
        try {
            // Lấy 20 thông báo mới nhất từ DAO
            List<Notification> all = dao.getLatestNotifications(20);

            LocalDate fromDate = dpFromDate.getValue();
            LocalDate toDate = dpToDate.getValue();
            String status = cbFilterStatus.getValue();

            List<Notification> sent = all.stream()
                    .filter(n -> n.getEmployeeID() == currentEmployeeID)
                    .filter(n -> filterByDate(n, fromDate, toDate))
                    .filter(n -> filterByStatus(n, status))
                    .sorted((a, b) -> b.getSentDate().compareTo(a.getSentDate())) // mới nhất lên đầu
                    .collect(Collectors.toList());

            List<Notification> received = all.stream()
                    .filter(n -> n.getReceiverID() != null && n.getReceiverID() == currentEmployeeID)
                    .filter(n -> filterByDate(n, fromDate, toDate))
                    .filter(n -> filterByStatus(n, status))
                    .sorted((a, b) -> b.getSentDate().compareTo(a.getSentDate())) // mới nhất lên đầu
                    .collect(Collectors.toList());

            tbSent.getItems().setAll(sent);
            tbReceived.getItems().setAll(received);

            System.out.println("Filter: Sent=" + sent.size() + ", Received=" + received.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean filterByDate(Notification n, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && n.getSentDate().toLocalDate().isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && n.getSentDate().toLocalDate().isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private boolean filterByStatus(Notification n, String status) {
        if (status == null) {
            return true;
        }
        if ("Read".equals(status)) {
            return n.isRead();
        }
        if ("Unread".equals(status)) {
            return !n.isRead();
        }
        return true;
    }

    @FXML
    private void onSend() {
        try {
            String title = txtTitle.getText();
            String content = txtContent.getText();
            String receiver = cbReceiver.getValue();

            if (title == null || title.isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Please enter a Title!").showAndWait();
                return;
            }
            if (content == null || content.isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Please enter Content!").showAndWait();
                return;
            }
            if (receiver == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a Receiver!").showAndWait();
                return;
            }

            Integer receiverID = null;
            if ("Manager".equals(receiver)) {
                receiverID = 1;
            } else if ("Cashier".equals(receiver)) {
                receiverID = 2;
            }

            Notification n = new Notification(
                    0,
                    currentEmployeeID,
                    receiverID,
                    "Current Employee", receiver,
                    title,
                    content,
                    LocalDateTime.now(),
                    false
            );

            dao.insertNotification(n);
            new Alert(Alert.AlertType.INFORMATION, "Notification sent successfully!").showAndWait();

            txtTitle.clear();
            txtContent.clear();
            cbReceiver.getSelectionModel().clearSelection();

            onFilter();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error while sending notification!").showAndWait();
        }
    }

    private void markAsRead(Notification notification) {
        try {
            notification.setRead(true);
            dao.updateNotificationStatus((int) notification.getNotificationID(), true);
            onFilter();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Error while marking notification as read!").showAndWait();
        }
    }

    // Hàm mở notiCard.fxml và truyền dữ liệu
    private void showNotificationCard(Notification n, boolean isReceived) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/Warehouse/notiCard.fxml"));
            AnchorPane pane = loader.load();

            controller.Warehouse.notiCardController cardController = loader.getController();
            cardController.setData(n, isReceived);

            Stage stage = new Stage();
            stage.setTitle("Notification Detail");
            stage.setScene(new Scene(pane));
            stage.show();

            // Nếu là Received và chưa đọc thì đánh dấu đã đọc
            if (isReceived && !n.isRead()) {
                markAsRead(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error while opening notification card!").showAndWait();
        }
    }
}
