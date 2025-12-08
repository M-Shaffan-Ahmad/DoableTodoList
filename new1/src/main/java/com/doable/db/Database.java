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
                // Create users table
                s.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL UNIQUE, " +
                        "password TEXT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "phone_number TEXT NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "department TEXT, " +
                        "job_title TEXT, " +
                        "created_by INTEGER, " +
                        "created_at INTEGER, " +
                        "FOREIGN KEY(created_by) REFERENCES users(id))");
                
                // Initialize admin user if not exists
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // Create default admin: username='admin', password='123'
                        s.execute("INSERT INTO users (username, password, email, phone_number, role, created_at) " +
                                "VALUES ('admin', '123', 'admin@company.com', '0000000000', 'ADMIN', " + System.currentTimeMillis() + ")");
                    }
                }
                
                // Create categories table
                s.execute("CREATE TABLE IF NOT EXISTS categories ("+
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "name TEXT NOT NULL UNIQUE, "+
                        "created_by INTEGER, " +
                        "FOREIGN KEY(created_by) REFERENCES users(id))");
                
                // Create tasks table with category_id
                s.execute("CREATE TABLE IF NOT EXISTS tasks ("+
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "title TEXT NOT NULL, "+
                        "description TEXT, "+
                        "due TEXT, "+
                        "completed INTEGER DEFAULT 0, "+
                        "repeat_rule TEXT DEFAULT 'NONE', "+
                        "category_id INTEGER, "+
                        "user_id INTEGER, " +
                        "created_by INTEGER, " +
                        "FOREIGN KEY(category_id) REFERENCES categories(id), " +
                        "FOREIGN KEY(user_id) REFERENCES users(id), " +
                        "FOREIGN KEY(created_by) REFERENCES users(id))");
                
                // Check if columns exist, if not add them
                try (ResultSet rs = s.executeQuery("PRAGMA table_info(tasks)")) {
                    boolean hasCategory = false;
                    boolean hasMarkedForCompletion = false;
                    boolean hasUserId = false;
                    boolean hasCreatedBy = false;
                    boolean hasAssignmentType = false;
                    boolean hasCreatedDate = false;
                    boolean hasDueDate = false;
                    while (rs.next()) {
                        if ("category_id".equals(rs.getString("name"))) {
                            hasCategory = true;
                        }
                        if ("marked_for_completion".equals(rs.getString("name"))) {
                            hasMarkedForCompletion = true;
                        }
                        if ("user_id".equals(rs.getString("name"))) {
                            hasUserId = true;
                        }
                        if ("created_by".equals(rs.getString("name"))) {
                            hasCreatedBy = true;
                        }
                        if ("assignment_type".equals(rs.getString("name"))) {
                            hasAssignmentType = true;
                        }
                        if ("created_date".equals(rs.getString("name"))) {
                            hasCreatedDate = true;
                        }
                        if ("due_date".equals(rs.getString("name"))) {
                            hasDueDate = true;
                        }
                    }
                    if (!hasCategory) {
                        s.execute("ALTER TABLE tasks ADD COLUMN category_id INTEGER");
                    }
                    if (!hasMarkedForCompletion) {
                        s.execute("ALTER TABLE tasks ADD COLUMN marked_for_completion INTEGER DEFAULT 0");
                    }
                    if (!hasUserId) {
                        s.execute("ALTER TABLE tasks ADD COLUMN user_id INTEGER");
                    }
                    if (!hasCreatedBy) {
                        s.execute("ALTER TABLE tasks ADD COLUMN created_by INTEGER");
                    }
                    if (!hasAssignmentType) {
                        s.execute("ALTER TABLE tasks ADD COLUMN assignment_type TEXT DEFAULT 'PERSONAL'");
                    }
                    if (!hasCreatedDate) {
                        s.execute("ALTER TABLE tasks ADD COLUMN created_date INTEGER DEFAULT " + System.currentTimeMillis());
                    }
                    if (!hasDueDate) {
                        s.execute("ALTER TABLE tasks ADD COLUMN due_date INTEGER");
                    }
                }
                
                // Check if categories table has created_by column
                try (ResultSet rs = s.executeQuery("PRAGMA table_info(categories)")) {
                    boolean hasCreatedBy = false;
                    while (rs.next()) {
                        if ("created_by".equals(rs.getString("name"))) {
                            hasCreatedBy = true;
                        }
                    }
                    if (!hasCreatedBy) {
                        s.execute("ALTER TABLE categories ADD COLUMN created_by INTEGER");
                    }
                }

                // Create assignments table to track task-employee relationships
                s.execute("CREATE TABLE IF NOT EXISTS assignments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "task_id INTEGER NOT NULL, " +
                        "employee_id INTEGER NOT NULL, " +
                        "assigned_by INTEGER NOT NULL, " +
                        "assigned_at INTEGER, " +
                        "marked_for_completion INTEGER DEFAULT 0, " +
                        "completed_at INTEGER, " +
                        "FOREIGN KEY(task_id) REFERENCES tasks(id), " +
                        "FOREIGN KEY(employee_id) REFERENCES users(id), " +
                        "FOREIGN KEY(assigned_by) REFERENCES users(id), " +
                        "UNIQUE(task_id, employee_id))");
                
                // Create action_logs table to track user actions
                s.execute("CREATE TABLE IF NOT EXISTS action_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "action_type TEXT NOT NULL, " +
                        "description TEXT, " +
                        "timestamp INTEGER, " +
                        "FOREIGN KEY(user_id) REFERENCES users(id))");
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
