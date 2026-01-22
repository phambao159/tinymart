package util;

import model.manager.employee.Employee;

public final class User {

    private static User instance;
    private Employee employee;

    private User(Employee employee) {
        this.employee = employee;
    }

    public static void setSession(Employee employee) {
        if (employee == null) {
            instance = null;
        } else {
            instance = new User(employee);
        }
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

    public Object getRole() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
