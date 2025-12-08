package com.doable.model;

/**
 * Admin user class - System administrator with full control
 */
public class Admin extends User {

    public Admin() {
        super();
        this.role = UserRole.ADMIN;
    }

    public Admin(String username, String password, String email, String phoneNumber) {
        super(username, password, email, phoneNumber, UserRole.ADMIN);
    }

    @Override
    public String toString() {
        return "Admin: " + username;
    }
}
