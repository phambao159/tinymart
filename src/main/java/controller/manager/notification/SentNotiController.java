package controller.manager.notification;

import dao.manager.notification.NotificationDAO;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.manager.notification.Notification;

public class SentNotiController implements Initializable {

    @FXML private HBox NotiTool;
    @FXML private ScrollPane viewSent;
    @FXML private FlowPane sentContainer;
    
    private final NotificationDAO nDAO = new NotificationDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSent();
    }

    @FXML
    private void onCreate(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/createNoti.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Create New Notification");
            stage.setScene(new Scene(root));
            
            // Đợi đóng popup xong mới thực thi tiếp (vì dùng showAndWait)
            stage.showAndWait(); 
            
            // Load lại danh sách sau khi tạo mới để thấy noti vừa gửi
            loadSent();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSent() {
        sentContainer.getChildren().clear(); 
        try {
            // Sử dụng hàm getSentNoti() - Giả sử SQL: WHERE EmployeeID = 1
            List<Notification> list = nDAO.getSentNoti(); 

            for (Notification n : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/notiCard.fxml"));
                Parent card = loader.load();

                NotiCardController cardController = loader.getController();

                // logic lấy tên người nhận (vì đây là mục Sent)
                String receiverName = "Staff ID: " + n.getReceiverID();
                
                cardController.setData(n, receiverName);
                
                // Khi click vào card đã gửi
                card.setOnMouseClicked(event -> {
                    showDetail(n, receiverName);
                });
                
                sentContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDetail(Notification n, String senderOrReceiver) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/detailNoti.fxml"));
            Parent root = loader.load();

            DetailNotiController controller = loader.getController();
            
            // TRUYỀN CALLBACK: Khi bên Detail bấm DELETE, hàm loadSent sẽ chạy lại
            controller.setDetailData(n, senderOrReceiver, () -> {
                loadSent();
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Notification Detail");
            stage.setScene(new Scene(root));
            
            // Khi đóng popup thông thường cũng nên refresh để cập nhật nếu có thay đổi
            stage.setOnHiding(e -> loadSent());

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch(ActionEvent event) {
        // Bạn có thể thêm logic search tương tự NotificationController ở đây
    }
}