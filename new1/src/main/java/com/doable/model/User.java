package com.doable.model;

/**
 * User class - Main user model with support for all roles
 * Contains all core user properties and role information
 */
public class User {
    protected long id;
    protected String username;
    protected String password;
    protected String email;
    protected String phoneNumber;
    protected long createdBy;    // User ID of who created this user
    protected long createdAt;    // Timestamp
    protected UserRole role;
    protected String department;  // For managers and employees
    protected String jobTitle;    // For employees

    public User() {}

    public User(String username, String password, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public User(String username, String password, String email, String phoneNumber, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public User(String username, String password, String email, String phoneNumber, UserRole role, String department) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.department = department;
    }

    public User(String username, String password, String email, String phoneNumber, UserRole role, String department, String jobTitle) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.department = department;
        this.jobTitle = jobTitle;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    @Override
    public String toString() {
        return username + " (" + (role != null ? role : "Unknown") + ")";
    }
}
