package com.doable.dao;

import com.doable.db.Database;
import com.doable.model.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeUserDao {
    private final Database db = Database.getInstance();

    public Employee save(Employee user) {
        try {
            Connection conn = db.getConnection();
            String sql = "INSERT INTO employee_users (username, password, email, phone_number, job_title, manager_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhoneNumber());
                ps.setString(5, user.getJobTitle());
                if (user.getManagerId() != null) {
                    ps.setInt(6, user.getManagerId());
                } else {
                    ps.setNull(6, Types.INTEGER);
                }
                ps.setLong(7, user.getCreatedAt() > 0 ? user.getCreatedAt() : System.currentTimeMillis());
                ps.executeUpdate();
                try (Statement s = conn.createStatement();
                     ResultSet rs = s.executeQuery("SELECT last_insert_rowid() as id")) {
                    if (rs.next()) user.setId(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public Employee findById(int id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM employee_users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Employee findByUsername(String username) {
        try {
            Connection conn = db.getConnection();
            String sql = "SELECT * FROM employee_users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Employee> findAll() {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM employee_users")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Employee> findByManagerId(int managerId) {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM employee_users WHERE manager_id = ?")) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void update(Employee user) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE employee_users SET username = ?, password = ?, email = ?, phone_number = ?, job_title = ?, manager_id = ? WHERE id = ?")) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getJobTitle());
            if (user.getManagerId() != null) {
                ps.setInt(6, user.getManagerId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM employee_users WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("job_title"),
                rs.getObject("manager_id") != null ? rs.getInt("manager_id") : null,
                rs.getLong("created_at")
        );
    }
}
