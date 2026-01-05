package controller.manager.notification;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import model.manager.notification.Notification;

/**
 * FXML Controller class
 *
 * @author user
 */
public class NotiCardController implements Initializable {

    @FXML
    private AnchorPane cardPane;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblSender;
    @FXML
    private Label lblContent;

    // Định dạng hiển thị ngày tháng: Ngày/Tháng/Năm Giờ:Phút
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Có thể thêm hiệu ứng hover hoặc thiết lập mặc định tại đây nếu cần
    }

    /**
     * Phương thức đổ dữ liệu từ Model vào UI
     *
     * @param noti Đối tượng thông báo
     * @param senderName Tên người gửi (lấy từ bảng Employee)
     */
    public void setData(Notification noti, String senderName) {
        lblTitle.setText(noti.getTitle());
        lblContent.setText(noti.getContent());
        lblSender.setText("From: " + senderName);

        // Kiểm tra và hiển thị ngày gửi
        if (noti.getSentDate() != null) {
            lblDate.setText(formatter.format(noti.getSentDate()));
        } else {
            lblDate.setText("N/A");
        }

        // Tùy biến giao diện dựa trên trạng thái đã đọc hay chưa
        if (!noti.isIsRead()) {
            // Nếu chưa đọc: làm nổi bật card (ví dụ: viền xanh, chữ đậm)
            cardPane.setStyle("-fx-background-color: #ffffff;");

        } else {
            // Nếu đã đọc: giao diện bình thường
            cardPane.setStyle("-fx-background-color: #e8e3e3;");

        }
    }
}
