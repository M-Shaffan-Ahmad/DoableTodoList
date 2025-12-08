package com.doable.model;

import java.time.LocalDateTime;

public class EmployeeTask {
    private long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private boolean completed;
    private String repeatRule;
    private long categoryId;
    private String categoryName;
    private boolean markedForCompletion;
    private int createdByEmployeeId;
    private String createdByEmployeeUsername;
    private long createdDate;
    private long dueDateTimestamp;

    public EmployeeTask() {}

    public EmployeeTask(long id, String title, String description, LocalDateTime dueDate, boolean completed,
                       String repeatRule, int createdByEmployeeId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
        this.repeatRule = repeatRule;
        this.createdByEmployeeId = createdByEmployeeId;
        this.categoryId = 0;
        this.categoryName = null;
        this.markedForCompletion = false;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getRepeatRule() { return repeatRule; }
    public void setRepeatRule(String repeatRule) { this.repeatRule = repeatRule; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public boolean isMarkedForCompletion() { return markedForCompletion; }
    public void setMarkedForCompletion(boolean markedForCompletion) { this.markedForCompletion = markedForCompletion; }

    public int getCreatedByEmployeeId() { return createdByEmployeeId; }
    public void setCreatedByEmployeeId(int createdByEmployeeId) { this.createdByEmployeeId = createdByEmployeeId; }

    public String getCreatedByEmployeeUsername() { return createdByEmployeeUsername; }
    public void setCreatedByEmployeeUsername(String createdByEmployeeUsername) { this.createdByEmployeeUsername = createdByEmployeeUsername; }

    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }

    public long getDueDateTimestamp() { return dueDateTimestamp; }
    public void setDueDateTimestamp(long dueDateTimestamp) { this.dueDateTimestamp = dueDateTimestamp; }

    @Override
    public String toString() {
        return title + (completed ? " (done)" : "");
    }
}
