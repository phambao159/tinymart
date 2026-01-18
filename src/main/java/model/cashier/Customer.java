package model.cashier;

import java.sql.Date;

public class Customer {

    private int id;
    private String fullName;
    private String phoneNumber;
    private int points;
    private String email;
    private Date registrationDate;

    public Customer(int id, String fullName, String phoneNumber, int points, String email, Date registrationDate) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.points = points;
        this.email = email;
        this.registrationDate = registrationDate;
    }

    public Customer(String fullName, String phoneNumber, String email) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.points = 0;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getEmail() {
        return email;
    }
}
