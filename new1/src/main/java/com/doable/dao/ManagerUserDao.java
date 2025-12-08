package com.doable.dao;

import com.doable.db.Database;
import com.doable.model.Manager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManagerUserDao {
    private final Database db = Database.getInstance();

    public Manager save(Manager user) {
        try {
            Connection conn = db.getConnection();
            String sql = "INSERT INTO manager_users (username, password, email, phone_number, department, admin_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhoneNumber());
                ps.setString(5, user.getDepartment());
                if (user.getAdminId() != null) {
                    ps.setInt(6, user.getAdminId());
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

    public Manager findById(int id) {
        try {
            Connection conn = db.getConnection();
            String sql = "SELECT * FROM manager_users WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Manager findByUsername(String username) {
        try {
            Connection conn = db.getConnection();
            String sql = "SELECT * FROM manager_users WHERE username = ?";
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

    public List<Manager> findAll() {
        List<Manager> list = new ArrayList<>();
        try {
            Connection conn = db.getConnection();
            String sql = "SELECT * FROM manager_users";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Manager> findByAdminId(int adminId) {
        List<Manager> list = new ArrayList<>();
        try {
            Connection conn = db.getConnection();
            String sql = "SELECT * FROM manager_users WHERE admin_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, adminId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void update(Manager user) {
        try {
            Connection conn = db.getConnection();
            String sql = "UPDATE manager_users SET username = ?, password = ?, email = ?, phone_number = ?, department = ?, admin_id = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhoneNumber());
                ps.setString(5, user.getDepartment());
                if (user.getAdminId() != null) {
                    ps.setInt(6, user.getAdminId());
                } else {
                    ps.setNull(6, Types.INTEGER);
                }
                ps.setInt(7, user.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int id) {
        try {
            Connection conn = db.getConnection();
            String sql = "DELETE FROM manager_users WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Manager mapRow(ResultSet rs) throws SQLException {
        return new Manager(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("department"),
                rs.getObject("admin_id") != null ? rs.getInt("admin_id") : null,
                rs.getLong("created_at")
        );
    }
}
