package controller.manager.notification;

import dao.manager.notification.NotificationDAO;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.manager.notification.Notification;

public class NotificationController implements Initializable {

    @FXML
    private HBox NotiTool;
    @FXML
    private FlowPane notiContainer;
    @FXML
    private ScrollPane viewNoti;
    @FXML
    private ToggleButton lbAll;
    @FXML
    private ToggleButton lbUnread;
    @FXML
    private ToggleGroup toggleNoti;
    @FXML
    private TextField tfSearch;

    private final NotificationDAO dao = new NotificationDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDataAndRender("all");
    }

    private void loadDataAndRender(String filter) {
        try {
            updateCountLabels();
            List<Notification> list;
            if (filter.equalsIgnoreCase("unread")) {
                list = dao.getData().stream()
                        .filter(n -> !n.isIsRead())
                        .collect(Collectors.toList());
            } else {
                list = dao.getData();
            }
            renderNotiCards(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderNotiCards(List<Notification> list) {
        notiContainer.getChildren().clear();
        try {
            for (Notification n : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/notiCard.fxml"));
                Parent card = loader.load();

                NotiCardController cardController = loader.getController();
                String senderName = (n.getEmployeeID() == 1) ? "Manager" : "Staff " + n.getEmployeeID();
                cardController.setData(n, senderName);

                // Gán sự kiện click trực tiếp vào Parent (card)
                card.setOnMouseClicked(event -> showDetail(n, senderName));

                notiContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCountLabels() {
        try {
            lbAll.setText(String.format("All (%d)", dao.countNoti("All")));
            lbUnread.setText(String.format("Unread (%d)", dao.countNoti("Unread")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDetail(Notification n, String senderName) {
        try {
            // 1. Cập nhật trạng thái "Đã đọc"
            if (!n.isIsRead()) {
                n.setIsRead(true);
                dao.update(n);
                updateCountLabels();
            }

            // 2. Load Popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/notification/detailNoti.fxml"));
            Parent root = loader.load();

            DetailNotiController controller = loader.getController();

            // TRUYỀN CALLBACK ĐỂ REFRESH KHI XÓA
            controller.setDetailData(n, senderName, () -> {
                String currentFilter = lbUnread.isSelected() ? "unread" : "all";
                loadDataAndRender(currentFilter);
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Notification Detail");
            stage.setScene(new Scene(root));

            // 3. Khi đóng Popup thì refresh lại (để cập nhật màu sắc card đã đọc)
            stage.setOnHiding(event -> {
                String currentFilter = lbUnread.isSelected() ? "unread" : "all";
                loadDataAndRender(currentFilter);
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onMark(ActionEvent event) {
        try {
            List<Notification> list = dao.getData();
            for (Notification n : list) {
                if (!n.isIsRead()) {
                    n.setIsRead(true);
                    dao.update(n);
                }
            }
            loadDataAndRender("all");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAll(ActionEvent event) {
        loadDataAndRender("all");
    }

    @FXML
    private void onUnread(ActionEvent event) {
        loadDataAndRender("unread");
    }

    @FXML
    private void onSearch(ActionEvent event) {
        String keyword = tfSearch.getText().trim();
        try {
            List<Notification> results = keyword.isEmpty() ? dao.getData() : dao.searchNotification(keyword);
            renderNotiCards(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
