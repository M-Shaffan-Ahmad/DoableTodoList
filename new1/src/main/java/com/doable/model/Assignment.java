package com.doable.model;

public class Assignment {
    private long id;
    private long taskId;
    private long employeeId;
    private long assignedBy;
    private long assignedAt;
    private boolean markedForCompletion;
    private long completedAt;

    public Assignment() {}

    public Assignment(long taskId, long employeeId, long assignedBy) {
        this.taskId = taskId;
        this.employeeId = employeeId;
        this.assignedBy = assignedBy;
        this.assignedAt = System.currentTimeMillis();
        this.markedForCompletion = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public long getEmployeeId() { return employeeId; }
    public void setEmployeeId(long employeeId) { this.employeeId = employeeId; }

    public long getAssignedBy() { return assignedBy; }
    public void setAssignedBy(long assignedBy) { this.assignedBy = assignedBy; }

    public long getAssignedAt() { return assignedAt; }
    public void setAssignedAt(long assignedAt) { this.assignedAt = assignedAt; }

    public boolean isMarkedForCompletion() { return markedForCompletion; }
    public void setMarkedForCompletion(boolean markedForCompletion) { this.markedForCompletion = markedForCompletion; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
}
