package util;

import model.manager.employee.Employee;

public final class User {

    private static User instance;
    private Employee employee; 
    private User(Employee employee) {
        this.employee = employee;
    }

    public static void setSession(Employee employee) {
        instance = new User(employee);
    }

    public static User getSession() {
        return instance;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void cleanUserSession() {
        employee = null;
    }
}