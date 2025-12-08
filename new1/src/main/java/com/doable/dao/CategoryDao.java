package com.doable.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.doable.db.Database;
import com.doable.model.Category;

public class CategoryDao {
    private final Connection conn;

    public CategoryDao() { this.conn = Database.getInstance().getConnection(); }

    public Category save(Category c) throws SQLException {
        if (c.getId() == 0) return insert(c);
        update(c);
        return c;
    }

    private Category insert(Category c) throws SQLException {
        String sql = "INSERT INTO categories(name, created_by) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            if (c.getCreatedBy() > 0) {
                ps.setLong(2, c.getCreatedBy());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.executeUpdate();
            // Get the last inserted ID for SQLite
            try (Statement stmt = conn.createStatement(); 
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) c.setId(rs.getLong(1));
            }
        }
        return c;
    }

    private void update(Category c) throws SQLException {
        String sql = "UPDATE categories SET name=?, created_by=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            if (c.getCreatedBy() > 0) {
                ps.setLong(2, c.getCreatedBy());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setLong(3, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public Category findById(long id) throws SQLException {
        String sql = "SELECT id, name, created_by FROM categories WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category c = new Category();
                    c.setId(rs.getLong("id"));
                    c.setName(rs.getString("name"));
                    c.setCreatedBy(rs.getLong("created_by"));
                    return c;
                }
            }
        }
        return null;
    }

    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, created_by FROM categories ORDER BY name";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getLong("id"));
                c.setName(rs.getString("name"));
                c.setCreatedBy(rs.getLong("created_by"));
                list.add(c);
            }
        }
        return list;
    }

    public boolean isCategoryNameExists(String name, long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories WHERE name = ? AND id != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
