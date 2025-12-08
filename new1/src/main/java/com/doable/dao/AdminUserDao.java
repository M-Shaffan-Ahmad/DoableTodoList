package com.doable.dao;

import com.doable.db.Database;
import com.doable.model.Admin;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminUserDao {
    private final Database db = Database.getInstance();

    public Admin save(Admin user) {
        try {
            Connection conn = db.getConnection();
            String sql = "INSERT INTO admin_users (username, password, email, phone_number, created_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhoneNumber());
                ps.setLong(5, user.getCreatedAt() > 0 ? user.getCreatedAt() : System.currentTimeMillis());
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

    public Admin findById(int id) {
        try {
            Connection conn = db.getConnection();
            String sql = "SELECT * FROM admin_users WHERE id = ?";
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

    public Admin findByUsername(String username) {
        try {
            Connection conn = db.getConnection();
            String sql = "SELECT * FROM admin_users WHERE username = ?";
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

    public List<Admin> findAll() {
        List<Admin> list = new ArrayList<>();
        try {
            Connection conn = db.getConnection();
            String sql = "SELECT * FROM admin_users";
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

    public void update(Admin user) {
        try {
            Connection conn = db.getConnection();
            String sql = "UPDATE admin_users SET username = ?, password = ?, email = ?, phone_number = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhoneNumber());
                ps.setInt(5, user.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int id) {
        try {
            Connection conn = db.getConnection();
            String sql = "DELETE FROM admin_users WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getLong("created_at")
        );
    }
}
