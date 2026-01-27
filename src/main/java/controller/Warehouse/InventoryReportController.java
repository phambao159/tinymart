package controller.Warehouse;

import dao.Warehouse.InventoryReportDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import model.Warehouse.InventoryReport;
import dao.Warehouse.NotificationDAO;
import model.Warehouse.Notification;
import java.time.LocalDateTime;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import util.DBConnection;

public class InventoryReportController implements Initializable {

    // Alert Cards container (khớp FXML)
    @FXML
    private FlowPane alertCardContainer;

    // Form tích hợp tạo Notification (khớp FXML)
    @FXML
    private TextField txtTitle;
    @FXML
    private TextArea txtContent;
    @FXML
    private ComboBox<String> cbReceiver;
    @FXML
    private javafx.scene.control.Button btnSend;

    private final InventoryReportDAO reportDAO = new InventoryReportDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[DEBUG] InventoryReportController initialized");

        // Load alert cards
        loadAlerts();

        // Thiết lập vùng nhận kéo thả cho txtContent
        setupDropTargetForContent();

        // (Tuỳ chọn) Khởi tạo danh sách người nhận
        initReceivers();
    }

    private void initReceivers() {
        // TODO: lấy danh sách receiver từ DAO/bảng Employee
        cbReceiver.getItems().setAll("Manager", "Cashier", "All");
    }

    /**
     * Hiển thị toàn bộ cảnh báo: - Các sản phẩm có stock thấp - Các sản phẩm có
     * hạn sử dụng gần nhất (Một sản phẩm có thể xuất hiện trong cả hai danh
     * sách nếu thỏa cả hai điều kiện)
     */
    private void loadAlerts() {
        try {
            System.out.println("[DEBUG] Loading alerts...");
            alertCardContainer.getChildren().clear();

            // Lấy danh sách sản phẩm từ DAO
            List<InventoryReport> nearestExpireList = reportDAO.getAllNearestExpireProducts();
            List<InventoryReport> lowStockList = reportDAO.getAllLowStockProducts();

            // Gom tất cả vào một list chung
            List<InventoryReport> alerts = new ArrayList<>();
            if (nearestExpireList != null && !nearestExpireList.isEmpty()) {
                alerts.addAll(nearestExpireList);
            }
            if (lowStockList != null && !lowStockList.isEmpty()) {
                alerts.addAll(lowStockList);
            }

            // Hiển thị từng card cảnh báo
            for (InventoryReport report : alerts) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Warehouse/InventoryReportCard.fxml"));
                Node cardRoot = loader.load();

                // Lấy controller của card để set dữ liệu
                InventoryReportCardController cardController = loader.getController();
                cardController.setData(report);

                // Gắn logic kéo lên node gốc của card (nếu có)
                attachDragFromCard(cardRoot, report);

                alertCardContainer.getChildren().add(cardRoot);
                System.out.println("[DEBUG] Added alert card for product: " + report.getProductName());
            }

            if (alerts.isEmpty()) {
                System.out.println("[DEBUG] No alerts found.");
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load alert card FXML: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in loadAlerts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cho phép kéo từ card: đưa text mô tả alert lên dragboard
     */
    private void attachDragFromCard(Node cardRoot, InventoryReport report) {
        cardRoot.setOnDragDetected(event -> {
            Dragboard db = cardRoot.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();

            // Chuẩn hoá text kéo thả: bạn có thể tuỳ biến format này
            String dragText = buildAlertText(report);
            content.putString(dragText);

            db.setContent(content);
            event.consume();
        });
    }

    /**
     * Vùng nhận thả: txtContent
     */
    private void setupDropTargetForContent() {
        txtContent.setOnDragOver(event -> {
            if (event.getGestureSource() != txtContent && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        txtContent.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                // Append nội dung alert vào content
                txtContent.appendText(db.getString() + "\n");
                // (Tuỳ chọn) Tự động gợi ý Title nếu đang trống
                if (txtTitle.getText() == null || txtTitle.getText().isBlank()) {
                    txtTitle.setText("Inventory Alert");
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Chuẩn hoá text khi kéo từ card (tuỳ biến theo dữ liệu bạn có)
     */
    private String buildAlertText(InventoryReport r) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Product] ").append(r.getProductName());
        if (r.getActionType() != null && !r.getActionType().isBlank()) {
            sb.append(" | Action: ").append(r.getActionType());
        }
        if (r.getExpireDate() != null && !r.getExpireDate().isBlank()) {
            sb.append(" | Expire: ").append(r.getExpireDate());
        }
        sb.append(" | Qty: ").append(r.getQuantity());
        return sb.toString();
    }

    @FXML
    private void onSend() {
        System.out.println("[DEBUG] Send button clicked");
        String title = txtTitle.getText();
        String content = txtContent.getText();
        String receiver = cbReceiver.getValue();
        System.out.println("[DEBUG] Title: " + title);
        System.out.println("[DEBUG] Content: " + content);
        System.out.println("[DEBUG] Receiver: " + receiver);
        // Validate
        if (title == null || title.isBlank() || content == null || content.isBlank() || receiver == null) {
            var warn = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            warn.setTitle("Warning");
            warn.setHeaderText("Incomplete Information");
            warn.setContentText("Please fill in Title, Content and Receiver before sending.");
            warn.showAndWait();
            return;
        }

        // Map receiver → EmployeeID (Manager=1, Cashier=2, Warehouse=3, All=null)
        Integer receiverID = mapReceiverToEmployeeId(receiver);
        System.out.println("[DEBUG] Mapped ReceiverID: " + receiverID);
        // Người gửi: Warehouse (3) — nếu bạn có session đăng nhập, thay bằng ID thực tế
        int employeeID = 3;

        // Tạo model Notification theo đúng class của bạn
        Notification n = new Notification(
                0L, // NotificationID (auto-increment, không cần set)
                employeeID, // EmployeeID (sender)
                receiverID, // ReceiverID (nullable nếu All)
                "Warehouse", // senderName (tuỳ bạn có join để hiển thị)
                receiver, // receiverName (tuỳ bạn có join để hiển thị)
                title,
                content,
                LocalDateTime.now(),
                false // IsRead
        );

        // Ghi DB bằng DAO của bạn (constructor nhận Connection, insert không trả ID)
        try (Connection conn = new DBConnection().getConnect()) {
            NotificationDAO dao = new NotificationDAO(conn);
            System.out.println("[DEBUG] Preparing to insert notification:");
            System.out.println("  SenderID: " + n.getEmployeeID());
            System.out.println("  ReceiverID: " + n.getReceiverID());
            System.out.println("  Title: " + n.getTitle());
            System.out.println("  Content: " + n.getContent());
            System.out.println("  SentDate: " + n.getSentDate());
            System.out.println("  IsRead: " + n.isRead());
            dao.insertNotification(n);
            var ok = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            ok.setTitle("Success");
            ok.setHeaderText("Notification Sent");
            ok.setContentText("Your notification has been sent successfully!");
            ok.showAndWait();

            // Reset form
            txtTitle.clear();
            txtContent.clear();
            cbReceiver.setValue(null);
        } catch (Exception e) {
            System.err.println("[ERROR] Exception: " + e.getClass().getSimpleName());
            System.err.println("[ERROR] Message: " + e.getMessage());
            var err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            err.setTitle("Error");
            err.setHeaderText("Failed to send notification");
            err.setContentText(e.getMessage());
            err.showAndWait();
        }
    }

    /**
     * Helper: map tên receiver → EmployeeID
     */
    private Integer mapReceiverToEmployeeId(String receiver) {
        Integer id = null;
        switch (receiver) {
            case "Manager":
                id = 1;
                break;
            case "Cashier":
                id = 2;
                break;
            case "Warehouse":
                id = 3;
                break;
            case "All":
                id = null;
                break;
            default:
                id = null;
                break;
        }
        return id;
    }
}
