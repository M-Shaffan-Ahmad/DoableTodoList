package com.doable.dao;

import com.doable.model.TaskReport;
import com.doable.model.ManagerReport;
import com.doable.db.Database;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class ReportDao {
    private static final String DB_URL = "jdbc:sqlite:doable.db";

    /**
     * Get task reports for a specific manager
     */
    public List<TaskReport> getTaskReportsByManager(long managerId) throws SQLException {
        List<TaskReport> reports = new ArrayList<>();
        String sql = "SELECT t.id, t.title, t.description, c.name as category, " +
                     "t.created_date, t.due_date, t.repeat_rule, t.created_by, u.username, " +
                     "COUNT(a.id) as total_assignments, " +
                     "SUM(CASE WHEN a.marked_for_completion = 1 THEN 1 ELSE 0 END) as completed_assignments " +
                     "FROM tasks t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "LEFT JOIN users u ON t.created_by = u.id " +
                     "LEFT JOIN assignments a ON t.id = a.task_id " +
                     "WHERE t.created_by = ? " +
                     "GROUP BY t.id " +
                     "ORDER BY t.created_date DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, managerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long taskId = rs.getLong("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String category = rs.getString("category") != null ? rs.getString("category") : "Uncategorized";
                
                long createdDateMs = rs.getLong("created_date");
                LocalDateTime createdDate = createdDateMs > 0 ? 
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(createdDateMs), ZoneId.systemDefault()) : null;
                
                long dueDateMs = rs.getLong("due_date");
                LocalDateTime dueDate = dueDateMs > 0 ? 
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(dueDateMs), ZoneId.systemDefault()) : null;
                
                String repeatRule = rs.getString("repeat_rule");
                long createdBy = rs.getLong("created_by");
                String createdByName = rs.getString("username");
                int totalAssignments = rs.getInt("total_assignments");
                int completedAssignments = rs.getInt("completed_assignments");

                TaskReport report = new TaskReport(taskId, title, description, category,
                        createdDate, dueDate, repeatRule, createdBy, createdByName,
                        totalAssignments, completedAssignments);
                reports.add(report);
            }
        }

        return reports;
    }

    /**
     * Get all task reports (for admin)
     */
    public List<TaskReport> getAllTaskReports() throws SQLException {
        List<TaskReport> reports = new ArrayList<>();
        String sql = "SELECT t.id, t.title, t.description, c.name as category, " +
                     "t.created_date, t.due_date, t.repeat_rule, t.created_by, u.username, " +
                     "COUNT(a.id) as total_assignments, " +
                     "SUM(CASE WHEN a.marked_for_completion = 1 THEN 1 ELSE 0 END) as completed_assignments " +
                     "FROM tasks t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "LEFT JOIN users u ON t.created_by = u.id " +
                     "LEFT JOIN assignments a ON t.id = a.task_id " +
                     "GROUP BY t.id " +
                     "ORDER BY t.created_date DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                long taskId = rs.getLong("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String category = rs.getString("category") != null ? rs.getString("category") : "Uncategorized";
                
                long createdDateMs = rs.getLong("created_date");
                LocalDateTime createdDate = createdDateMs > 0 ? 
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(createdDateMs), ZoneId.systemDefault()) : null;
                
                long dueDateMs = rs.getLong("due_date");
                LocalDateTime dueDate = dueDateMs > 0 ? 
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(dueDateMs), ZoneId.systemDefault()) : null;
                
                String repeatRule = rs.getString("repeat_rule");
                long createdBy = rs.getLong("created_by");
                String createdByName = rs.getString("username");
                int totalAssignments = rs.getInt("total_assignments");
                int completedAssignments = rs.getInt("completed_assignments");

                TaskReport report = new TaskReport(taskId, title, description, category,
                        createdDate, dueDate, repeatRule, createdBy, createdByName,
                        totalAssignments, completedAssignments);
                reports.add(report);
            }
        }

        return reports;
    }

    /**
     * Get manager reports for all managers (for admin)
     */
    public List<ManagerReport> getManagerReports() throws SQLException {
        List<ManagerReport> reports = new ArrayList<>();
        String sql = "SELECT " +
                     "u.id, u.username, " +
                     "COUNT(DISTINCT t.id) as total_tasks_created, " +
                     "COUNT(DISTINCT a.id) as total_assignments, " +
                     "SUM(CASE WHEN a.marked_for_completion = 1 THEN 1 ELSE 0 END) as completed_assignments, " +
                     "SUM(CASE WHEN a.marked_for_completion = 0 THEN 1 ELSE 0 END) as pending_assignments, " +
                     "COUNT(DISTINCT a.employee_id) as total_employees, " +
                     "MAX(COALESCE(al.timestamp, t.created_date)) as last_activity " +
                     "FROM users u " +
                     "LEFT JOIN tasks t ON u.id = t.created_by " +
                     "LEFT JOIN assignments a ON t.id = a.task_id " +
                     "LEFT JOIN action_logs al ON u.id = al.user_id " +
                     "WHERE u.role = 'MANAGER' " +
                     "GROUP BY u.id, u.username " +
                     "ORDER BY total_tasks_created DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                long managerId = rs.getLong("id");
                String managerName = rs.getString("username");
                int totalTasksCreated = rs.getInt("total_tasks_created");
                int totalAssignments = rs.getInt("total_assignments");
                int completedAssignments = rs.getInt("completed_assignments");
                int pendingAssignments = rs.getInt("pending_assignments");
                int totalEmployees = rs.getInt("total_employees");
                
                long lastActivityMs = rs.getLong("last_activity");
                LocalDateTime lastActivity = lastActivityMs > 0 ? 
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(lastActivityMs), ZoneId.systemDefault()) : null;

                ManagerReport report = new ManagerReport(managerId, managerName, totalTasksCreated,
                        totalAssignments, completedAssignments, pendingAssignments, totalEmployees, lastActivity);
                reports.add(report);
            }
        }

        return reports;
    }

    /**
     * Get manager report for a specific manager (for admin detail view)
     */
    public ManagerReport getManagerReport(long managerId) throws SQLException {
        String sql = "SELECT " +
                     "u.id, u.username, " +
                     "COUNT(DISTINCT t.id) as total_tasks_created, " +
                     "COUNT(DISTINCT a.id) as total_assignments, " +
                     "SUM(CASE WHEN a.marked_for_completion = 1 THEN 1 ELSE 0 END) as completed_assignments, " +
                     "SUM(CASE WHEN a.marked_for_completion = 0 THEN 1 ELSE 0 END) as pending_assignments, " +
                     "COUNT(DISTINCT a.employee_id) as total_employees, " +
                     "MAX(COALESCE(al.timestamp, t.created_date)) as last_activity " +
                     "FROM users u " +
                     "LEFT JOIN tasks t ON u.id = t.created_by " +
                     "LEFT JOIN assignments a ON t.id = a.task_id " +
                     "LEFT JOIN action_logs al ON u.id = al.user_id " +
                     "WHERE u.id = ? AND u.role = 'MANAGER' " +
                     "GROUP BY u.id, u.username";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, managerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                long id = rs.getLong("id");
                String managerName = rs.getString("username");
                int totalTasksCreated = rs.getInt("total_tasks_created");
                int totalAssignments = rs.getInt("total_assignments");
                int completedAssignments = rs.getInt("completed_assignments");
                int pendingAssignments = rs.getInt("pending_assignments");
                int totalEmployees = rs.getInt("total_employees");
                
                long lastActivityMs = rs.getLong("last_activity");
                LocalDateTime lastActivity = lastActivityMs > 0 ? 
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(lastActivityMs), ZoneId.systemDefault()) : null;

                return new ManagerReport(id, managerName, totalTasksCreated,
                        totalAssignments, completedAssignments, pendingAssignments, totalEmployees, lastActivity);
            }
        }

        return null;
    }

    /**
     * Get task statistics summary for a manager
     */
    public Map<String, Object> getTaskStatistics(long managerId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                     "COUNT(DISTINCT t.id) as total_tasks, " +
                     "COUNT(DISTINCT a.id) as total_assignments, " +
                     "SUM(CASE WHEN a.marked_for_completion = 1 THEN 1 ELSE 0 END) as completed, " +
                     "SUM(CASE WHEN a.marked_for_completion = 0 THEN 1 ELSE 0 END) as pending, " +
                     "COUNT(DISTINCT CASE WHEN a.marked_for_completion = 1 THEN a.employee_id END) as employees_completed " +
                     "FROM tasks t " +
                     "LEFT JOIN assignments a ON t.id = a.task_id " +
                     "WHERE t.created_by = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, managerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.put("totalTasks", rs.getInt("total_tasks"));
                stats.put("totalAssignments", rs.getInt("total_assignments"));
                stats.put("completed", rs.getInt("completed"));
                stats.put("pending", rs.getInt("pending"));
                stats.put("employeesWithCompletions", rs.getInt("employees_completed"));
            }
        }

        return stats;
    }

    /**
     * Get category-wise task breakdown for a manager
     */
    public Map<String, Integer> getCategoryBreakdown(long managerId) throws SQLException {
        Map<String, Integer> breakdown = new LinkedHashMap<>();
        String sql = "SELECT COALESCE(c.name, 'Uncategorized') as category, COUNT(DISTINCT t.id) as count " +
                     "FROM tasks t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.created_by = ? " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY count DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, managerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                breakdown.put(rs.getString("category"), rs.getInt("count"));
            }
        }

        return breakdown;
    }
}
