package com.doable.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Database {
    private static Database INSTANCE;
    private Connection conn;
    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private Database() {}

    public static Database getInstance() {
        if (INSTANCE == null) INSTANCE = new Database();
        return INSTANCE;
    }

    public void init() {
        try {
            String url = "jdbc:sqlite:doable.db";
            conn = DriverManager.getConnection(url);
            try (Statement s = conn.createStatement()) {
                // Create categories table
                s.execute("CREATE TABLE IF NOT EXISTS categories ("+
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "name TEXT NOT NULL UNIQUE, "+
                        "color TEXT DEFAULT '#3b82f6')");
                
                // Create tasks table with category_id
                s.execute("CREATE TABLE IF NOT EXISTS tasks ("+
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "title TEXT NOT NULL, "+
                        "description TEXT, "+
                        "due TEXT, "+
                        "completed INTEGER DEFAULT 0, "+
                        "repeat_rule TEXT DEFAULT 'NONE', "+
                        "category_id INTEGER, "+
                        "FOREIGN KEY(category_id) REFERENCES categories(id))");
                
                // Check if category_id column exists, if not add it
                try (ResultSet rs = s.executeQuery("PRAGMA table_info(tasks)")) {
                    boolean hasCategory = false;
                    boolean hasMarkedForCompletion = false;
                    while (rs.next()) {
                        if ("category_id".equals(rs.getString("name"))) {
                            hasCategory = true;
                        }
                        if ("marked_for_completion".equals(rs.getString("name"))) {
                            hasMarkedForCompletion = true;
                        }
                    }
                    if (!hasCategory) {
                        s.execute("ALTER TABLE tasks ADD COLUMN category_id INTEGER");
                    }
                    if (!hasMarkedForCompletion) {
                        s.execute("ALTER TABLE tasks ADD COLUMN marked_for_completion INTEGER DEFAULT 0");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() { return conn; }

    public void close() {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
    }

    // helper to convert LocalDateTime <-> string
    public String toString(LocalDateTime dt) {
        return dt == null ? null : dt.format(fmt);
    }
    public LocalDateTime fromString(String s) {
        return s == null ? null : LocalDateTime.parse(s, fmt);
    }
}
