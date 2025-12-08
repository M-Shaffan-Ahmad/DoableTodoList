package com.doable.model;

public class ActionLog {
    private long id;
    private long userId;
    private String actionType;  // CREATE_TASK, ASSIGN_TASK, UNASSIGN_TASK, COMPLETE_TASK, CREATE_CATEGORY, etc.
    private String description;
    private long timestamp;

    public ActionLog() {
    }

    public ActionLog(long userId, String actionType, String description) {
        this.userId = userId;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ActionLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", actionType='" + actionType + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
