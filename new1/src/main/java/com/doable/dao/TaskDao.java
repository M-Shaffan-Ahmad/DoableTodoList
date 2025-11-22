package com.doable.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.doable.db.Database;
import com.doable.model.Task;

public class TaskDao {
    private final Connection conn;

    public TaskDao() { this.conn = Database.getInstance().getConnection(); }

    public Task save(Task t) throws SQLException {
        if (t.getId() == 0) return insert(t);
        update(t);
        return t;
    }

    private Task insert(Task t) throws SQLException {
        String sql = "INSERT INTO tasks(title, description, due, completed, repeat_rule, category_id, marked_for_completion) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            ps.setString(3, Database.getInstance().toString(t.getDueDate()));
            ps.setInt(4, t.isCompleted() ? 1 : 0);
            ps.setString(5, t.getRepeatRule());
            if (t.getCategoryId() > 0) {
                ps.setLong(6, t.getCategoryId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            ps.setInt(7, t.isMarkedForCompletion() ? 1 : 0);
            ps.executeUpdate();
            // Get the last inserted ID for SQLite
            try (Statement stmt = conn.createStatement(); 
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) t.setId(rs.getLong(1));
            }
        }
        return t;
    }

    private void update(Task t) throws SQLException {
        String sql = "UPDATE tasks SET title=?, description=?, due=?, completed=?, repeat_rule=?, category_id=?, marked_for_completion=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            ps.setString(3, Database.getInstance().toString(t.getDueDate()));
            ps.setInt(4, t.isCompleted() ? 1 : 0);
            ps.setString(5, t.getRepeatRule());
            if (t.getCategoryId() > 0) {
                ps.setLong(6, t.getCategoryId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            ps.setInt(7, t.isMarkedForCompletion() ? 1 : 0);
            ps.setLong(8, t.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public List<Task> findAll() throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT t.id, t.title, t.description, t.due, t.completed, t.repeat_rule, t.category_id, t.marked_for_completion, c.name " +
                    "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id " +
                    "ORDER BY t.due IS NULL, t.due";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Task t = new Task();
                t.setId(rs.getLong("id"));
                t.setTitle(rs.getString("title"));
                t.setDescription(rs.getString("description"));
                t.setDueDate(Database.getInstance().fromString(rs.getString("due")));
                t.setCompleted(rs.getInt("completed") == 1);
                t.setRepeatRule(rs.getString("repeat_rule"));
                t.setMarkedForCompletion(rs.getInt("marked_for_completion") == 1);
                long catId = rs.getLong("category_id");
                if (catId > 0) {
                    t.setCategoryId(catId);
                    t.setCategoryName(rs.getString("name"));
                }
                list.add(t);
            }
        }
        return list;
    }
}
