package model.Warehouse;

import java.time.LocalDateTime;

public class Notification {
    private long notificationID;     // bigint → long
    private int employeeID;          // int → int
    private Integer receiverID;      // int → Integer (nullable)
    private String senderName;
    private String receiverName;
    private String title;
    private String content;
    private LocalDateTime sentDate;  // datetime → LocalDateTime
    private boolean isRead;          // tinyint(1) → boolean

    public Notification(long notificationID, int employeeID, Integer receiverID,
                        String senderName, String receiverName,
                        String title, String content,
                        LocalDateTime sentDate, boolean isRead) {
        this.notificationID = notificationID;
        this.employeeID = employeeID;
        this.receiverID = receiverID;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.title = title;
        this.content = content;
        this.sentDate = sentDate;
        this.isRead = isRead;
    }

    // Getters & Setters
    public long getNotificationID() { return notificationID; }
    public void setNotificationID(long notificationID) { this.notificationID = notificationID; }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public Integer getReceiverID() { return receiverID; }
    public void setReceiverID(Integer receiverID) { this.receiverID = receiverID; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentDate() { return sentDate; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}