package com.doable.model;

public class Category {
    private long id;
    private String name;
    private String color; // hex color code like #FF5733

    public Category() {}

    public Category(String name, String color) {
        this.name = name;
        this.color = color;
    }
    
    public Category(long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public String toString() { return name; }
}
