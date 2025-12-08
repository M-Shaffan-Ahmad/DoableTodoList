package com.doable.dao;

import com.doable.db.Database;
import com.doable.model.ManagerTask;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ManagerTaskDao {
    private final Database db = Database.getInstance();

    public ManagerTask save(ManagerTask task) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO manager_tasks (title, description, due, completed, repeat_rule, category_id, created_by_manager_id, created_date, due_date, marked_for_completion) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getDueDate() != null ? task.getDueDate().toString() : null);
            ps.setBoolean(4, task.isCompleted());
            ps.setString(5, task.getRepeatRule());
            ps.setLong(6, task.getCategoryId() > 0 ? task.getCategoryId() : 0);
            ps.setInt(7, task.getCreatedByManagerId());
            ps.setLong(8, task.getCreatedDate() > 0 ? task.getCreatedDate() : System.currentTimeMillis());
            ps.setLong(9, task.getDueDateTimestamp() > 0 ? task.getDueDateTimestamp() : 0);
            ps.setBoolean(10, task.isMarkedForCompletion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) task.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return task;
    }

    public ManagerTask findById(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT t.*, c.name FROM manager_tasks t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<ManagerTask> findAll() {
        List<ManagerTask> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT t.*, c.name FROM manager_tasks t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "ORDER BY t.created_date DESC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<ManagerTask> findByManagerId(int managerId) {
        List<ManagerTask> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT t.*, c.name FROM manager_tasks t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.created_by_manager_id = ? " +
                     "ORDER BY t.created_date DESC")) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<ManagerTask> findByManagerIdAndCompleted(int managerId, boolean completed) {
        List<ManagerTask> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT t.*, c.name FROM manager_tasks t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.created_by_manager_id = ? AND t.completed = ? " +
                     "ORDER BY t.created_date DESC")) {
            ps.setInt(1, managerId);
            ps.setBoolean(2, completed);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void update(ManagerTask task) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE manager_tasks SET title = ?, description = ?, due = ?, completed = ?, " +
                     "repeat_rule = ?, category_id = ?, due_date = ?, marked_for_completion = ? WHERE id = ?")) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getDueDate() != null ? task.getDueDate().toString() : null);
            ps.setBoolean(4, task.isCompleted());
            ps.setString(5, task.getRepeatRule());
            ps.setLong(6, task.getCategoryId() > 0 ? task.getCategoryId() : 0);
            ps.setLong(7, task.getDueDateTimestamp() > 0 ? task.getDueDateTimestamp() : 0);
            ps.setBoolean(8, task.isMarkedForCompletion());
            ps.setLong(9, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM manager_tasks WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ManagerTask mapRow(ResultSet rs) throws SQLException {
        ManagerTask task = new ManagerTask();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        String dueStr = rs.getString("due");
        if (dueStr != null && !dueStr.isEmpty()) {
            task.setDueDate(LocalDateTime.parse(dueStr));
        }
        task.setCompleted(rs.getBoolean("completed"));
        task.setRepeatRule(rs.getString("repeat_rule"));
        task.setCategoryId(rs.getLong("category_id"));
        task.setCategoryName(rs.getString("name"));
        task.setMarkedForCompletion(rs.getBoolean("marked_for_completion"));
        task.setCreatedByManagerId(rs.getInt("created_by_manager_id"));
        task.setCreatedDate(rs.getLong("created_date"));
        task.setDueDateTimestamp(rs.getLong("due_date"));
        return task;
    }
}
