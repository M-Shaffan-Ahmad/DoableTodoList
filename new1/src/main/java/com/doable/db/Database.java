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
            System.out.println("DEBUG: Connected to SQLite database");
            try (Statement s = conn.createStatement()) {
                // Create unified users table for all roles
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
                        "created_at INTEGER)");
                System.out.println("DEBUG: Users table created/verified");
                
                // Create admin users table
                s.execute("CREATE TABLE IF NOT EXISTS admin_users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL UNIQUE, " +
                        "password TEXT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "phone_number TEXT NOT NULL, " +
                        "created_at INTEGER)");
                
                // Create manager users table
                s.execute("CREATE TABLE IF NOT EXISTS manager_users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL UNIQUE, " +
                        "password TEXT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "phone_number TEXT NOT NULL, " +
                        "department TEXT, " +
                        "admin_id INTEGER, " +
                        "created_at INTEGER, " +
                        "FOREIGN KEY(admin_id) REFERENCES admin_users(id))");
                
                // Create employee users table
                s.execute("CREATE TABLE IF NOT EXISTS employee_users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL UNIQUE, " +
                        "password TEXT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "phone_number TEXT NOT NULL, " +
                        "job_title TEXT, " +
                        "manager_id INTEGER, " +
                        "created_at INTEGER, " +
                        "FOREIGN KEY(manager_id) REFERENCES manager_users(id))");
                
                // Initialize default users if not exists
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("DEBUG: Creating default users");
                        
                        // Create admin user
                        s.execute("INSERT INTO admin_users (username, password, email, phone_number, created_at) " +
                                "VALUES ('admin', '123', 'admin@company.com', '0000000000', " + System.currentTimeMillis() + ")");
                        s.execute("INSERT INTO users (username, password, email, phone_number, role, created_at) " +
                                "VALUES ('admin', '123', 'admin@company.com', '0000000000', 'ADMIN', " + System.currentTimeMillis() + ")");
                        System.out.println("DEBUG: Admin user created");
                        
                        // Create manager user
                        s.execute("INSERT INTO manager_users (username, password, email, phone_number, department, created_at) " +
                                "VALUES ('manager', '123', 'manager@company.com', '1111111111', 'IT', " + System.currentTimeMillis() + ")");
                        s.execute("INSERT INTO users (username, password, email, phone_number, role, department, created_at) " +
                                "VALUES ('manager', '123', 'manager@company.com', '1111111111', 'MANAGER', 'IT', " + System.currentTimeMillis() + ")");
                        System.out.println("DEBUG: Manager user created");
                        
                        // Create employee user
                        s.execute("INSERT INTO employee_users (username, password, email, phone_number, job_title, created_at) " +
                                "VALUES ('employee', '123', 'employee@company.com', '2222222222', 'Developer', " + System.currentTimeMillis() + ")");
                        s.execute("INSERT INTO users (username, password, email, phone_number, role, job_title, created_at) " +
                                "VALUES ('employee', '123', 'employee@company.com', '2222222222', 'EMPLOYEE', 'Developer', " + System.currentTimeMillis() + ")");
                        System.out.println("DEBUG: Employee user created");
                        
                    } else {
                        System.out.println("DEBUG: Default users already exist");
                    }
                }
                
                // Create categories table
                s.execute("CREATE TABLE IF NOT EXISTS categories ("+
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "name TEXT NOT NULL UNIQUE, "+
                        "created_by INTEGER)");
                
                // Create unified tasks table
                s.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT, " +
                        "due TEXT, " +
                        "completed INTEGER DEFAULT 0, " +
                        "repeat_rule TEXT DEFAULT 'NONE', " +
                        "category_id INTEGER, " +
                        "marked_for_completion INTEGER DEFAULT 0, " +
                        "user_id INTEGER, " +
                        "created_by INTEGER, " +
                        "assignment_type TEXT DEFAULT 'PERSONAL', " +
                        "FOREIGN KEY(category_id) REFERENCES categories(id))");
                
                // Create manager_tasks table
                s.execute("CREATE TABLE IF NOT EXISTS manager_tasks ("+
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "title TEXT NOT NULL, "+
                        "description TEXT, "+
                        "due TEXT, "+
                        "completed INTEGER DEFAULT 0, "+
                        "repeat_rule TEXT DEFAULT 'NONE', "+
                        "category_id INTEGER, "+
                        "created_by_manager_id INTEGER NOT NULL, " +
                        "created_date INTEGER, " +
                        "due_date INTEGER, " +
                        "marked_for_completion INTEGER DEFAULT 0, " +
                        "FOREIGN KEY(category_id) REFERENCES categories(id), " +
                        "FOREIGN KEY(created_by_manager_id) REFERENCES manager_users(id))");
                
                // Create employee_tasks table
                s.execute("CREATE TABLE IF NOT EXISTS employee_tasks ("+
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        "title TEXT NOT NULL, "+
                        "description TEXT, "+
                        "due TEXT, "+
                        "completed INTEGER DEFAULT 0, "+
                        "repeat_rule TEXT DEFAULT 'NONE', "+
                        "category_id INTEGER, "+
                        "created_by_employee_id INTEGER NOT NULL, " +
                        "created_date INTEGER, " +
                        "due_date INTEGER, " +
                        "marked_for_completion INTEGER DEFAULT 0, " +
                        "FOREIGN KEY(category_id) REFERENCES categories(id), " +
                        "FOREIGN KEY(created_by_employee_id) REFERENCES employee_users(id))");
                
                // Create assignments table to track task-employee relationships
                s.execute("CREATE TABLE IF NOT EXISTS assignments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "task_id INTEGER NOT NULL, " +
                        "employee_id INTEGER NOT NULL, " +
                        "assigned_by INTEGER NOT NULL, " +
                        "assigned_at INTEGER, " +
                        "marked_for_completion INTEGER DEFAULT 0, " +
                        "completed_at INTEGER DEFAULT 0, " +
                        "FOREIGN KEY(task_id) REFERENCES manager_tasks(id), " +
                        "FOREIGN KEY(employee_id) REFERENCES employee_users(id), " +
                        "FOREIGN KEY(assigned_by) REFERENCES manager_users(id))");
                
                // Create action_logs table to track user actions
                s.execute("CREATE TABLE IF NOT EXISTS action_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "action_type TEXT NOT NULL, " +
                        "description TEXT, " +
                        "timestamp INTEGER, " +
                        "FOREIGN KEY(user_id) REFERENCES users(id))");
            }
            // Migrate old schema if needed
            migrateAssignmentsTableSchema();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void migrateAssignmentsTableSchema() {
        try {
            // Check if assignments table has the old schema
            DatabaseMetaData dbMeta = conn.getMetaData();
            ResultSet columns = dbMeta.getColumns(null, null, "assignments", null);
            boolean hasOldSchema = false;
            boolean hasMissingMarkedForCompletion = false;
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if ("manager_task_id".equals(columnName) || "assigned_by_manager_id".equals(columnName)) {
                    hasOldSchema = true;
                }
                if ("marked_for_completion".equals(columnName)) {
                    hasMissingMarkedForCompletion = false;
                }
            }
            columns.close();
            
            if (hasOldSchema) {
                System.out.println("DEBUG: Detected old assignments table schema, migrating...");
                try (Statement s = conn.createStatement()) {
                    // Create backup of old data
                    s.execute("CREATE TABLE assignments_backup AS SELECT * FROM assignments");
                    
                    // Drop old table
                    s.execute("DROP TABLE assignments");
                    
                    // Create new table with correct schema
                    s.execute("CREATE TABLE assignments (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "task_id INTEGER NOT NULL, " +
                            "employee_id INTEGER NOT NULL, " +
                            "assigned_by INTEGER NOT NULL, " +
                            "assigned_at INTEGER, " +
                            "marked_for_completion INTEGER DEFAULT 0, " +
                            "completed_at INTEGER DEFAULT 0, " +
                            "FOREIGN KEY(task_id) REFERENCES manager_tasks(id), " +
                            "FOREIGN KEY(employee_id) REFERENCES employee_users(id), " +
                            "FOREIGN KEY(assigned_by) REFERENCES manager_users(id))");
                    
                    // Restore data with proper column mapping
                    s.execute("INSERT INTO assignments (id, task_id, employee_id, assigned_by, assigned_at, completed_at) " +
                            "SELECT id, COALESCE(manager_task_id, 0) as task_id, employee_id, assigned_by_manager_id, assigned_at, completed_at " +
                            "FROM assignments_backup");
                    
                    // Drop backup table
                    s.execute("DROP TABLE assignments_backup");
                    
                    System.out.println("DEBUG: Successfully migrated assignments table schema");
                }
            }
            
            // Migrate action_logs table if it has old schema
            columns = dbMeta.getColumns(null, null, "action_logs", null);
            boolean actionLogsHasOldSchema = false;
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if ("admin_user_id".equals(columnName) || "manager_user_id".equals(columnName) || "employee_user_id".equals(columnName)) {
                    actionLogsHasOldSchema = true;
                }
            }
            columns.close();
            
            if (actionLogsHasOldSchema) {
                System.out.println("DEBUG: Detected old action_logs table schema, migrating...");
                try (Statement s = conn.createStatement()) {
                    // Create backup of old data
                    s.execute("CREATE TABLE action_logs_backup AS SELECT * FROM action_logs");
                    
                    // Drop old table
                    s.execute("DROP TABLE action_logs");
                    
                    // Create new table with correct schema
                    s.execute("CREATE TABLE action_logs (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "user_id INTEGER, " +
                            "action_type TEXT NOT NULL, " +
                            "description TEXT, " +
                            "timestamp INTEGER, " +
                            "FOREIGN KEY(user_id) REFERENCES users(id))");
                    
                    // Restore data with proper column mapping (use admin_user_id if available)
                    s.execute("INSERT INTO action_logs (id, user_id, action_type, description, timestamp) " +
                            "SELECT id, COALESCE(admin_user_id, manager_user_id, employee_user_id, 0) as user_id, action_type, description, timestamp " +
                            "FROM action_logs_backup");
                    
                    // Drop backup table
                    s.execute("DROP TABLE action_logs_backup");
                    
                    System.out.println("DEBUG: Successfully migrated action_logs table schema");
                }
            }
        } catch (SQLException e) {
            System.err.println("Migration warning (non-fatal): " + e.getMessage());
            // Don't throw - if migration fails, continue with fresh table
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
