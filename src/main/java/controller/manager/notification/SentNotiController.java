package controller.manager.notification;

import dao.manager.employee.EmployeeDAO;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.manager.employee.Employee;
import model.manager.notification.Notification;

public class SentNotiController implements Initializable {

    @FXML private ScrollPane viewSent;
    @FXML private VBox sentContainer;
    @FXML private TextField tfSearch;

    private final NotificationDAO nDAO = new NotificationDAO();
    private final EmployeeDAO eDAO = new EmployeeDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSent();
        
        // Tính năng tìm kiếm real-time (khi đang gõ)
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                performSearch(newValue);
            });
        }
    }

    /**
     * Tải toàn bộ danh sách thông báo đã gửi
     */
    private void loadSent() {
        try {
            List<Notification> list = nDAO.getSentNoti(); 
            renderNotiCards(list);
        } catch (Exception e) {
            System.err.println("Lỗi load danh sách đã gửi: " + e.getMessage());
        }
    }

    /**
     * Hiển thị danh sách Notification lên giao diện VBox
     */
    private void renderNotiCards(List<Notification> list) {
        sentContainer.getChildren().clear();
        
        if (list == null || list.isEmpty()) {
            return;
        }

        for (Notification n : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/notiCard.fxml"));
                Parent card = loader.load();

                // Lấy thông tin người nhận
                Employee receiver = eDAO.getEmployeeById(n.getReceiverID());
                String displayName = (receiver != null) 
                        ? "To: " + receiver.getRole() + " " + receiver.getFullName() 
                        : "To: Unknown User";

                // Setup Controller của Card
                NotiCardController cardController = loader.getController();
                cardController.setData(n, displayName);

                // Sự kiện xem chi tiết
                card.setOnMouseClicked(event -> showDetail(n, displayName));

                sentContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onCreate(ActionEvent event) {
        openPopup("/manager/notification/createNoti.fxml", "Create New Notification");
    }

    /**
     * Xử lý tìm kiếm thủ công (khi bấm nút Search)
     */
    @FXML
    private void onSearch(ActionEvent event) {
        performSearch(tfSearch.getText().trim());
    }

    private void performSearch(String keyword) {
        try {
            List<Notification> results;
            if (keyword == null || keyword.isEmpty()) {
                results = nDAO.getSentNoti();
            } else {
                results = nDAO.searchsentNotification(keyword); 
            }
            renderNotiCards(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDetail(Notification n, String receiverInfo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/detailNoti.fxml"));
            Parent root = loader.load();

            DetailNotiController controller = loader.getController();
            
            // Truyền Callback để refresh khi xóa/sửa thành công
            controller.setDetailData(n, receiverInfo, this::loadSent);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Notification Detail");
            stage.setScene(new Scene(root));
            
            // Refresh sau khi đóng detail đề phòng có thay đổi trạng thái
            stage.setOnHidden(e -> loadSent()); 
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hàm tiện ích mở Popup và refresh danh sách khi đóng
     */
    private void openPopup(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadSent(); // Refresh sau khi đóng popup
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}