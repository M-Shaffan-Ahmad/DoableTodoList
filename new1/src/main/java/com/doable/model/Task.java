package com.doable.model;

import java.time.LocalDateTime;

public class Task {
    private long id;
    private String title;
    private String description;
    private LocalDateTime dueDate; // nullable
    private boolean completed;
    private String repeatRule; // e.g., "NONE", "DAILY", "WEEKLY"
    private long categoryId; // nullable
    private String categoryName; // nullable
    private boolean markedForCompletion; // Track if task is checked for completion (to suppress reminders)

    public Task() {}

    public Task(long id, String title, String description, LocalDateTime dueDate, boolean completed, String repeatRule) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
        this.repeatRule = repeatRule;
        this.categoryId = 0;
        this.categoryName = null;
        this.markedForCompletion = false;
    }

    // getters and setters
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

    @Override
    public String toString() {
        return title + (completed ? " (done)" : "");
    }
}
