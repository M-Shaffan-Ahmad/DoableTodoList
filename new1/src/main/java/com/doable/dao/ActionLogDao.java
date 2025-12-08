package com.doable.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.doable.db.Database;
import com.doable.model.ActionLog;

public class ActionLogDao {
    private final Connection conn;

    public ActionLogDao() {
        this.conn = Database.getInstance().getConnection();
    }

    public ActionLog save(ActionLog log) throws SQLException {
        if (log.getId() == 0) return insert(log);
        update(log);
        return log;
    }

    private ActionLog insert(ActionLog log) throws SQLException {
        String sql = "INSERT INTO action_logs(user_id, action_type, description, timestamp) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, log.getUserId());
            ps.setString(2, log.getActionType());
            ps.setString(3, log.getDescription());
            ps.setLong(4, log.getTimestamp());
            ps.executeUpdate();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) log.setId(rs.getLong(1));
            }
        }
        return log;
    }

    private void update(ActionLog log) throws SQLException {
        String sql = "UPDATE action_logs SET user_id=?, action_type=?, description=?, timestamp=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, log.getUserId());
            ps.setString(2, log.getActionType());
            ps.setString(3, log.getDescription());
            ps.setLong(4, log.getTimestamp());
            ps.setLong(5, log.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM action_logs WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public ActionLog findById(long id) throws SQLException {
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM action_logs WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ActionLog log = new ActionLog();
                    log.setId(rs.getLong("id"));
                    log.setUserId(rs.getLong("user_id"));
                    log.setActionType(rs.getString("action_type"));
                    log.setDescription(rs.getString("description"));
                    log.setTimestamp(rs.getLong("timestamp"));
                    return log;
                }
            }
        }
        return null;
    }

    public List<ActionLog> findByUserId(long userId) throws SQLException {
        List<ActionLog> list = new ArrayList<>();
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM action_logs WHERE user_id=? ORDER BY timestamp DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActionLog log = new ActionLog();
                    log.setId(rs.getLong("id"));
                    log.setUserId(rs.getLong("user_id"));
                    log.setActionType(rs.getString("action_type"));
                    log.setDescription(rs.getString("description"));
                    log.setTimestamp(rs.getLong("timestamp"));
                    list.add(log);
                }
            }
        }
        return list;
    }

    public List<ActionLog> findByActionType(String actionType) throws SQLException {
        List<ActionLog> list = new ArrayList<>();
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM action_logs WHERE action_type=? ORDER BY timestamp DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actionType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActionLog log = new ActionLog();
                    log.setId(rs.getLong("id"));
                    log.setUserId(rs.getLong("user_id"));
                    log.setActionType(rs.getString("action_type"));
                    log.setDescription(rs.getString("description"));
                    log.setTimestamp(rs.getLong("timestamp"));
                    list.add(log);
                }
            }
        }
        return list;
    }

    public List<ActionLog> findAll() throws SQLException {
        List<ActionLog> list = new ArrayList<>();
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM action_logs ORDER BY timestamp DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActionLog log = new ActionLog();
                    log.setId(rs.getLong("id"));
                    log.setUserId(rs.getLong("user_id"));
                    log.setActionType(rs.getString("action_type"));
                    log.setDescription(rs.getString("description"));
                    log.setTimestamp(rs.getLong("timestamp"));
                    list.add(log);
                }
            }
        }
        return list;
    }
}
