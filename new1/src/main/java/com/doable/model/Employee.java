package com.doable.model;

/**
 * Employee user class - Regular employee with task management capabilities
 */
public class Employee extends User {

    public Employee() {
        super();
        this.role = UserRole.EMPLOYEE;
    }

    public Employee(String username, String password, String email, String phoneNumber) {
        super(username, password, email, phoneNumber, UserRole.EMPLOYEE);
    }

    public Employee(String username, String password, String email, String phoneNumber, String jobTitle, String department) {
        super(username, password, email, phoneNumber, UserRole.EMPLOYEE, department, jobTitle);
    }

    @Override
    public String toString() {
        return "Employee: " + username + " (" + (jobTitle != null ? jobTitle : "No Title") + ")";
    }
}
