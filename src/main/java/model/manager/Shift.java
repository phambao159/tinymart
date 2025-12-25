package model.manager;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Model class for Shift table
 * Ref: tinymart.sql schema
 */
public class Shift {

    private int shiftID;           // ShiftID int(10) UNSIGNED
    private LocalTime startTime; // StartTime datetime
    private LocalTime endTime;   // EndTime datetime
    private String shiftName;      // ShiftName varchar(255)

    // Constructor mặc định
    public Shift() {
    }

    // Constructor đầy đủ (dùng khi lấy dữ liệu từ database)
    public Shift(int shiftID, LocalTime startTime, LocalTime endTime, String shiftName) {
        this.shiftID = shiftID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftName = shiftName;
    }

    // Constructor không có ID (dùng khi thêm mới vào database - ID tự tăng)
    public Shift(LocalTime startTime, LocalTime endTime, String shiftName) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftName = shiftName;
    }

    // Getter và Setter
    public int getShiftID() {
        return shiftID;
    }

    public void setShiftID(int shiftID) {
        this.shiftID = shiftID;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "shiftID=" + shiftID +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", shiftName='" + shiftName + '\'' +
                '}';
    }
}