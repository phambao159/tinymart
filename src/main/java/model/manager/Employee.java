package model.manager;

import java.time.LocalDate;

public class Employee {


    private int employeeID;
    private String fullName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String address;
    private String role;
    private LocalDate hireDate;
    private long baseSalary;
    private String user;
    private String password;


    public Employee() {
    }


    public Employee(int employeeID, String fullName, LocalDate dateOfBirth, String phoneNumber, String address, String role, LocalDate hireDate, long baseSalary, String user, String password) {
        this.employeeID = employeeID;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.hireDate = hireDate;
        this.baseSalary = baseSalary;
        this.user = user;
        this.password = password;
    }


    public Employee(String fullName, LocalDate dateOfBirth, String phoneNumber, String address, String role, LocalDate hireDate, long baseSalary, String user, String password) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.hireDate = hireDate;
        this.baseSalary = baseSalary;
        this.user = user;
        this.password = password;
    }
    


    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public long getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(long baseSalary) {
        this.baseSalary = baseSalary;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}