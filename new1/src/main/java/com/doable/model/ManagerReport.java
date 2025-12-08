package com.doable.model;

import java.time.LocalDateTime;

/**
 * Model class to represent manager activity reporting data for admins
 */
public class ManagerReport {
    private long managerId;
    private String managerName;
    private int totalTasksCreated;
    private int totalTasksAssigned;
    private int totalAssignmentsCompleted;
    private int totalAssignmentsPending;
    private double overallCompletionPercentage;
    private int totalEmployeesManaged;
    private int activeTasks;
    private LocalDateTime lastActivityDate;
    private String performanceStatus;

    // Constructor
    public ManagerReport(long managerId, String managerName, int totalTasksCreated,
                        int totalTasksAssigned, int totalAssignmentsCompleted, 
                        int totalAssignmentsPending, int totalEmployeesManaged,
                        LocalDateTime lastActivityDate) {
        this.managerId = managerId;
        this.managerName = managerName;
        this.totalTasksCreated = totalTasksCreated;
        this.totalTasksAssigned = totalTasksAssigned;
        this.totalAssignmentsCompleted = totalAssignmentsCompleted;
        this.totalAssignmentsPending = totalAssignmentsPending;
        this.totalEmployeesManaged = totalEmployeesManaged;
        this.lastActivityDate = lastActivityDate;
        this.activeTasks = totalTasksAssigned - totalAssignmentsCompleted;
        
        int totalAssignments = totalAssignmentsCompleted + totalAssignmentsPending;
        this.overallCompletionPercentage = totalAssignments > 0 ? 
            (totalAssignmentsCompleted * 100.0 / totalAssignments) : 0;
        
        // Determine performance status
        if (overallCompletionPercentage >= 80) {
            this.performanceStatus = "Excellent";
        } else if (overallCompletionPercentage >= 60) {
            this.performanceStatus = "Good";
        } else if (overallCompletionPercentage >= 40) {
            this.performanceStatus = "Fair";
        } else {
            this.performanceStatus = "Needs Improvement";
        }
    }

    // Getters
    public long getManagerId() {
        return managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public int getTotalTasksCreated() {
        return totalTasksCreated;
    }

    public int getTotalTasksAssigned() {
        return totalTasksAssigned;
    }

    public int getTotalAssignmentsCompleted() {
        return totalAssignmentsCompleted;
    }

    public int getTotalAssignmentsPending() {
        return totalAssignmentsPending;
    }

    public double getOverallCompletionPercentage() {
        return overallCompletionPercentage;
    }

    public int getTotalEmployeesManaged() {
        return totalEmployeesManaged;
    }

    public int getActiveTasks() {
        return activeTasks;
    }

    public LocalDateTime getLastActivityDate() {
        return lastActivityDate;
    }

    public String getPerformanceStatus() {
        return performanceStatus;
    }

    // Formatted getters
    public String getFormattedCompletionPercentage() {
        return String.format("%.1f%%", overallCompletionPercentage);
    }

    public String getFormattedLastActivityDate() {
        return lastActivityDate != null ? 
            lastActivityDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
    }

    public String getSummary() {
        return String.format("%s - Created: %d tasks, Assigned: %d, Completed: %d/%d (%.1f%%)", 
            managerName, totalTasksCreated, totalTasksAssigned, 
            totalAssignmentsCompleted, totalAssignmentsPending + totalAssignmentsCompleted, 
            overallCompletionPercentage);
    }
}
