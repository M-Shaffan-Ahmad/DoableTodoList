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
        String sql = "INSERT INTO categories(name, color) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getColor());
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
        String sql = "UPDATE categories SET name=?, color=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getColor());
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
        String sql = "SELECT id, name, color FROM categories WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category c = new Category();
                    c.setId(rs.getLong("id"));
                    c.setName(rs.getString("name"));
                    c.setColor(rs.getString("color"));
                    return c;
                }
            }
        }
        return null;
    }

    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, color FROM categories ORDER BY name";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getLong("id"));
                c.setName(rs.getString("name"));
                c.setColor(rs.getString("color"));
                list.add(c);
            }
        }
        return list;
    }
}
