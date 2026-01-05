package model.manager.notification;

import java.util.Date;

/**
 * Model class for Notification
 * @author user
 */
public class Notification {
    private long notificationID;
    private int employeeID;
    private int receiverID;
    private String title;
    private String content;
    private Date sentDate;
    private boolean isRead;

    // Constructor không tham số
    public Notification() {
    }

    // Constructor đầy đủ tham số
    public Notification(long notificationID, int employeeID, int receiverID, String title, String content, Date sentDate, boolean isRead) {
        this.notificationID = notificationID;
        this.employeeID = employeeID;
        this.receiverID = receiverID;
        this.title = title;
        this.content = content;
        this.sentDate = sentDate;
        this.isRead = isRead;
    }

    // Getter và Setter
    public long getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(long notificationID) {
        this.notificationID = notificationID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    // Ghi đè toString để thuận tiện cho việc debug
    @Override
    public String toString() {
        return "Notification{" +
                "notificationID=" + notificationID +
                ", employeeID=" + employeeID +
                ", receiverID=" + receiverID +
                ", title='" + title + '\'' +
                ", sentDate=" + sentDate +
                ", isRead=" + isRead +
                '}';
    }
}