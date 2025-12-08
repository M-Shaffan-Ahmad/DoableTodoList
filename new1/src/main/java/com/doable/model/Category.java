package com.doable.model;

public class Category {
    private long id;
    private String name;
    private long createdBy; // Who created this category (user_id of creator)
    private String createdByUsername; // Username of category creator

    public Category() {}

    public Category(String name) {
        this.name = name;
    }
    
    public Category(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    @Override
    public String toString() { return name; }
}
