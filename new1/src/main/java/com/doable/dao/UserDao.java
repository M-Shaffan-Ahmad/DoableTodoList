package com.doable.dao;

import com.doable.db.Database;
import com.doable.model.User;
import com.doable.model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final Database DB = Database.getInstance();

    // Authenticate user
    public static User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = DB.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    System.out.println("DEBUG: User authenticated - " + user.getUsername() + " (ID=" + user.getId() + ", Role=" + user.getRole() + ")");
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("DEBUG: Authentication failed for username: " + username);
        return null;
    }

    // Find user by username
    public static User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = DB.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Find user by ID
    public static User findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = DB.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Create a new user
    public static User createUser(User user) {
        // Check if username already exists
        if (findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        String sql = "INSERT INTO users (username, password, email, phone_number, role, department, job_title, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DB.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.getDepartment());
            ps.setString(7, user.getJobTitle());
            ps.setLong(8, user.getCreatedBy());
            ps.setLong(9, user.getCreatedAt());
            ps.executeUpdate();

            // Get the last inserted ID for SQLite
            try (Statement s = DB.getConnection().createStatement();
                 ResultSet rs = s.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
        return user;
    }

    // Get all managers
    public static List<User> getAllManagers() {
        String sql = "SELECT * FROM users WHERE role = 'MANAGER'";
        return executeQuery(sql);
    }

    // Get all employees
    public static List<User> getAllEmployees() {
        String sql = "SELECT * FROM users WHERE role = 'EMPLOYEE'";
        return executeQuery(sql);
    }

    // Get all users for debugging
    public static List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        List<User> users = executeQuery(sql);
        System.out.println("DEBUG: Total users in database: " + users.size());
        for (User u : users) {
            System.out.println("  - " + u.getUsername() + " (ID=" + u.getId() + ", Role=" + u.getRole() + ")");
        }
        return users;
    }

    // Get employees created by a specific manager
    public static List<User> getEmployeesByManager(long managerId) {
        String sql = "SELECT * FROM users WHERE role = 'EMPLOYEE' AND created_by = ?";
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = DB.getConnection().prepareStatement(sql)) {
            ps.setLong(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Update user
    public static void updateUser(User user) {
        String sql = "UPDATE users SET password = ?, email = ?, phone_number = ?, department = ?, job_title = ? WHERE id = ?";
        try (PreparedStatement ps = DB.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getPassword());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhoneNumber());
            ps.setString(4, user.getDepartment());
            ps.setString(5, user.getJobTitle());
            ps.setLong(6, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete user
    public static void deleteUser(long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = DB.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to execute query
    private static List<User> executeQuery(String sql) {
        List<User> users = new ArrayList<>();
        try (Statement s = DB.getConnection().createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Map ResultSet to User object
    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setRole(UserRole.fromString(rs.getString("role")));
        user.setDepartment(rs.getString("department"));
        user.setJobTitle(rs.getString("job_title"));
        user.setCreatedBy(rs.getLong("created_by"));
        user.setCreatedAt(rs.getLong("created_at"));
        return user;
    }
}
