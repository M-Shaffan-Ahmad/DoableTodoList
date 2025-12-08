package com.doable.model;

import java.time.LocalDateTime;

/**
 * Model class to represent task reporting data for managers and admins
 */
public class TaskReport {
    private long taskId;
    private String taskTitle;
    private String description;
    private String category;
    private LocalDateTime createdDate;
    private LocalDateTime dueDate;
    private String repeatRule;
    private long createdBy;
    private String createdByName;
    private int totalAssignments;
    private int completedAssignments;
    private int pendingAssignments;
    private double completionPercentage;
    private String status;

    // Constructor
    public TaskReport(long taskId, String taskTitle, String description, String category, 
                     LocalDateTime createdDate, LocalDateTime dueDate, String repeatRule,
                     long createdBy, String createdByName, int totalAssignments, 
                     int completedAssignments) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.description = description;
        this.category = category;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.repeatRule = repeatRule;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.totalAssignments = totalAssignments;
        this.completedAssignments = completedAssignments;
        this.pendingAssignments = totalAssignments - completedAssignments;
        this.completionPercentage = totalAssignments > 0 ? (completedAssignments * 100.0 / totalAssignments) : 0;
        this.status = completionPercentage == 100 ? "Completed" : (completionPercentage > 0 ? "In Progress" : "Pending");
    }

    // Getters
    public long getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public String getRepeatRule() {
        return repeatRule;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public int getTotalAssignments() {
        return totalAssignments;
    }

    public int getCompletedAssignments() {
        return completedAssignments;
    }

    public int getPendingAssignments() {
        return pendingAssignments;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public String getStatus() {
        return status;
    }

    // Formatted getters
    public String getFormattedCreatedDate() {
        return createdDate != null ? createdDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
    }

    public String getFormattedDueDate() {
        return dueDate != null ? dueDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";
    }

    public String getCompletionPercentageFormatted() {
        return String.format("%.1f%%", completionPercentage);
    }
}
