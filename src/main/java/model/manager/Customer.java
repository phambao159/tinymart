package model.manager;

import java.time.LocalDate;

public class Customer {
    private int customerID;
    private String fullName;
    private String phoneNumber;
    private int points;
    private String email;
    private LocalDate registrationDate;

    public Customer() {
    }

    // Constructor đầy đủ
    public Customer(int customerID, String fullName, String phoneNumber, int points, String email, LocalDate registrationDate) {
        this.customerID = customerID;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.points = points;
        this.email = email;
        this.registrationDate = registrationDate;
    }

    // Getter và Setter
    public int getCustomerID() { return customerID; }
    public void setCustomerID(int customerID) { this.customerID = customerID; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
}