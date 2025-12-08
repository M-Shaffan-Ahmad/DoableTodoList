package com.doable.model;

/**
 * Manager user class - Manages employees and tasks
 */
public class Manager extends User {
    
    public Manager() {
        super();
        this.role = UserRole.MANAGER;
    }

    public Manager(String username, String password, String email, String phoneNumber) {
        super(username, password, email, phoneNumber, UserRole.MANAGER);
    }

    public Manager(String username, String password, String email, String phoneNumber, String department) {
        super(username, password, email, phoneNumber, UserRole.MANAGER, department);
    }

    @Override
    public String toString() {
        return "Manager: " + username + " (" + (department != null ? department : "No Dept") + ")";
    }
}
