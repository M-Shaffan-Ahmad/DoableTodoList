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
        System.out.println("DEBUG TaskDao.insert: Inserting task - title=" + t.getTitle() + ", userId=" + t.getUserId() + ", createdBy=" + t.getCreatedBy() + ", assignmentType=" + t.getAssignmentType());
        String sql = "INSERT INTO tasks(title, description, due, completed, repeat_rule, category_id, marked_for_completion, user_id, created_by, assignment_type) VALUES(?,?,?,?,?,?,?,?,?,?)";
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
            if (t.getUserId() > 0) {
                ps.setLong(8, t.getUserId());
                System.out.println("DEBUG: Setting userId=" + t.getUserId());
            } else {
                ps.setNull(8, Types.BIGINT);
                System.out.println("DEBUG: Setting userId to NULL");
            }
            if (t.getCreatedBy() > 0) {
                ps.setLong(9, t.getCreatedBy());
            } else {
                ps.setNull(9, Types.BIGINT);
            }
            ps.setString(10, t.getAssignmentType() != null ? t.getAssignmentType() : "PERSONAL");
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
        String sql = "UPDATE tasks SET title=?, description=?, due=?, completed=?, repeat_rule=?, category_id=?, marked_for_completion=?, user_id=?, created_by=?, assignment_type=? WHERE id=?";
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
            if (t.getUserId() > 0) {
                ps.setLong(8, t.getUserId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            if (t.getCreatedBy() > 0) {
                ps.setLong(9, t.getCreatedBy());
            } else {
                ps.setNull(9, Types.BIGINT);
            }
            ps.setString(10, t.getAssignmentType() != null ? t.getAssignmentType() : "PERSONAL");
            ps.setLong(11, t.getId());
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
        String sql = "SELECT t.id, t.title, t.description, t.due, t.completed, t.repeat_rule, t.category_id, t.marked_for_completion, t.user_id, t.created_by, t.assignment_type, c.name " +
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
                long userId = rs.getLong("user_id");
                if (userId > 0) {
                    t.setUserId(userId);
                }
                long createdBy = rs.getLong("created_by");
                if (createdBy > 0) {
                    t.setCreatedBy(createdBy);
                }
                t.setAssignmentType(rs.getString("assignment_type"));
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

    public boolean isTitleExists(String title, long excludeTaskId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE title = ? AND id != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setLong(2, excludeTaskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean isTitleExists(String title) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE title = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Task> findByCreatedBy(long createdBy) throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT t.id, t.title, t.description, t.due, t.completed, t.repeat_rule, t.category_id, t.marked_for_completion, t.user_id, t.created_by, t.assignment_type, c.name " +
                    "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id " +
                    "WHERE t.created_by = ? " +
                    "ORDER BY t.due IS NULL, t.due";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, createdBy);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task t = new Task();
                    t.setId(rs.getLong("id"));
                    t.setTitle(rs.getString("title"));
                    t.setDescription(rs.getString("description"));
                    t.setDueDate(Database.getInstance().fromString(rs.getString("due")));
                    t.setCompleted(rs.getInt("completed") == 1);
                    t.setRepeatRule(rs.getString("repeat_rule"));
                    t.setMarkedForCompletion(rs.getInt("marked_for_completion") == 1);
                    long userId = rs.getLong("user_id");
                    if (userId > 0) {
                        t.setUserId(userId);
                    }
                    long createdByValue = rs.getLong("created_by");
                    if (createdByValue > 0) {
                        t.setCreatedBy(createdByValue);
                    }
                    t.setAssignmentType(rs.getString("assignment_type"));
                    long catId = rs.getLong("category_id");
                    if (catId > 0) {
                        t.setCategoryId(catId);
                        t.setCategoryName(rs.getString("name"));
                    }
                    list.add(t);
                }
            }
        }
        return list;
    }

    public List<Task> findByAssignedTo(long userId) throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT t.id, t.title, t.description, t.due, t.completed, t.repeat_rule, t.category_id, t.marked_for_completion, t.user_id, t.created_by, t.assignment_type, c.name " +
                    "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id " +
                    "WHERE t.user_id = ? " +
                    "ORDER BY t.due IS NULL, t.due";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task t = new Task();
                    t.setId(rs.getLong("id"));
                    t.setTitle(rs.getString("title"));
                    t.setDescription(rs.getString("description"));
                    t.setDueDate(Database.getInstance().fromString(rs.getString("due")));
                    t.setCompleted(rs.getInt("completed") == 1);
                    t.setRepeatRule(rs.getString("repeat_rule"));
                    t.setMarkedForCompletion(rs.getInt("marked_for_completion") == 1);
                    long userIdValue = rs.getLong("user_id");
                    if (userIdValue > 0) {
                        t.setUserId(userIdValue);
                    }
                    long createdByValue = rs.getLong("created_by");
                    if (createdByValue > 0) {
                        t.setCreatedBy(createdByValue);
                    }
                    t.setAssignmentType(rs.getString("assignment_type"));
                    long catId = rs.getLong("category_id");
                    if (catId > 0) {
                        t.setCategoryId(catId);
                        t.setCategoryName(rs.getString("name"));
                    }
                    list.add(t);
                }
            }
        }
        return list;
    }

    public Task findById(long id) throws SQLException {
        String sql = "SELECT t.id, t.title, t.description, t.due, t.completed, t.repeat_rule, t.category_id, t.marked_for_completion, t.user_id, t.created_by, t.assignment_type, c.name " +
                    "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id " +
                    "WHERE t.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Task t = new Task();
                    t.setId(rs.getLong("id"));
                    t.setTitle(rs.getString("title"));
                    t.setDescription(rs.getString("description"));
                    t.setDueDate(Database.getInstance().fromString(rs.getString("due")));
                    t.setCompleted(rs.getInt("completed") == 1);
                    t.setRepeatRule(rs.getString("repeat_rule"));
                    t.setMarkedForCompletion(rs.getInt("marked_for_completion") == 1);
                    long userIdValue = rs.getLong("user_id");
                    if (userIdValue > 0) {
                        t.setUserId(userIdValue);
                    }
                    long createdByValue = rs.getLong("created_by");
                    if (createdByValue > 0) {
                        t.setCreatedBy(createdByValue);
                    }
                    t.setAssignmentType(rs.getString("assignment_type"));
                    long catId = rs.getLong("category_id");
                    if (catId > 0) {
                        t.setCategoryId(catId);
                        t.setCategoryName(rs.getString("name"));
                    }
                    return t;
                }
            }
        }
        return null;
    }
}
