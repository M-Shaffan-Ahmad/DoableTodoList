package com.doable.dao;

import com.doable.db.Database;
import com.doable.model.Assignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDao {
    private final Connection conn;

    public AssignmentDao() {
        this.conn = Database.getInstance().getConnection();
    }

    public Assignment save(Assignment assignment) throws SQLException {
        if (assignment.getId() == 0) return insert(assignment);
        update(assignment);
        return assignment;
    }

    private Assignment insert(Assignment a) throws SQLException {
        String sql = "INSERT INTO assignments(task_id, employee_id, assigned_by, assigned_at, marked_for_completion, completed_at) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, a.getTaskId());
            ps.setLong(2, a.getEmployeeId());
            ps.setLong(3, a.getAssignedBy());
            ps.setLong(4, a.getAssignedAt());
            ps.setInt(5, a.isMarkedForCompletion() ? 1 : 0);
            ps.setLong(6, a.getCompletedAt());
            ps.executeUpdate();
            // Get the last inserted ID
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) a.setId(rs.getLong(1));
            }
        }
        System.out.println("DEBUG AssignmentDao.insert: Created assignment - taskId=" + a.getTaskId() + ", employeeId=" + a.getEmployeeId());
        return a;
    }

    private void update(Assignment a) throws SQLException {
        String sql = "UPDATE assignments SET marked_for_completion=?, completed_at=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.isMarkedForCompletion() ? 1 : 0);
            ps.setLong(2, a.getCompletedAt());
            ps.setLong(3, a.getId());
            ps.executeUpdate();
            System.out.println("DEBUG AssignmentDao.update: Updated assignment ID=" + a.getId() + ", markedForCompletion=" + a.isMarkedForCompletion());
        }
    }

    public Assignment findById(long id) throws SQLException {
        String sql = "SELECT * FROM assignments WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAssignment(rs);
                }
            }
        }
        return null;
    }

    public List<Assignment> findByTaskId(long taskId) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT * FROM assignments WHERE task_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapResultSetToAssignment(rs));
                }
            }
        }
        return assignments;
    }

    public List<Assignment> findByEmployeeId(long employeeId) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT * FROM assignments WHERE employee_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapResultSetToAssignment(rs));
                }
            }
        }
        return assignments;
    }

    public List<Assignment> findByManagerId(long managerId) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();
        String sql = "SELECT * FROM assignments WHERE assigned_by = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapResultSetToAssignment(rs));
                }
            }
        }
        return assignments;
    }

    public Assignment findByTaskAndEmployee(long taskId, long employeeId) throws SQLException {
        String sql = "SELECT * FROM assignments WHERE task_id = ? AND employee_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, taskId);
            ps.setLong(2, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAssignment(rs);
                }
            }
        }
        return null;
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM assignments WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Assignment mapResultSetToAssignment(ResultSet rs) throws SQLException {
        Assignment a = new Assignment();
        a.setId(rs.getLong("id"));
        a.setTaskId(rs.getLong("task_id"));
        a.setEmployeeId(rs.getLong("employee_id"));
        a.setAssignedBy(rs.getLong("assigned_by"));
        a.setAssignedAt(rs.getLong("assigned_at"));
        a.setMarkedForCompletion(rs.getInt("marked_for_completion") == 1);
        a.setCompletedAt(rs.getLong("completed_at"));
        return a;
    }
}
