package controller.manager.notification;

import dao.manager.notification.NotificationDAO;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.manager.notification.Notification;

public class DetailNotiController implements Initializable {

    @FXML private Label lblTitle;
    @FXML private Label lblSender;
    @FXML private Label lblDate;
    @FXML private TextArea txtContent;

    private final NotificationDAO nDAO = new NotificationDAO();
    private Notification currentNoti; // Lưu lại để dùng cho hàm Delete
    private Runnable onRefreshCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * Hàm nhận dữ liệu từ card bên ngoài
     */
    public void setDetailData(Notification n, String senderName,Runnable refreshCallback) {
        this.currentNoti = n; // Gán giá trị để hàm onDelete có ID mà xóa
        this.onRefreshCallback = refreshCallback;
        
        lblTitle.setText(n.getTitle());
        lblSender.setText(senderName);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        lblDate.setText(sdf.format(n.getSentDate()));
        
        txtContent.setText(n.getContent());
    }

    @FXML
    private void onClose(ActionEvent event) {
        closeWindow();
    }

    @FXML
    private void onDelete(ActionEvent event) {
        if (currentNoti == null) return;

        // 1. Hiển thị xác nhận xóa
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this notification?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // 2. Gọi DAO để xóa
                    nDAO.delete(currentNoti.getNotificationID());
                    if (onRefreshCallback != null) {
                        onRefreshCallback.run();
                    }
   
                    closeWindow();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Could not delete notification: " + e.getMessage());
                }
            }
        });
    }

    private void closeWindow() {
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }
}