package controller.Warehouse;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Warehouse.Notification;

import java.time.format.DateTimeFormatter;

public class notiCardController {

    @FXML private Label lblTitle;
    @FXML private Label lblDate;
    @FXML private Label lblSender;
    @FXML private Label lblContent;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setData(Notification n, boolean isReceived) {
        lblTitle.setText(n.getTitle());
        lblDate.setText(n.getSentDate().format(fmt));
        lblContent.setText(n.getContent());

        if (isReceived) {
            lblSender.setText("Người gửi: " + n.getSenderName());
        } else {
            lblSender.setText("Người nhận: " + n.getReceiverName());
        }
    }
}