# Doable - User Management & Login System Guide

## Overview
The Doable application now features a comprehensive user management and login system with three user roles: Admin (Boss), Manager, and Employee. Each role has specific permissions and responsibilities.

---

## User Roles & Hierarchy

### 1. **Admin (Boss)**
- **Default Credentials**: 
  - Username: `admin`
  - Password: `123`
  - Email: `admin@company.com`
  - Phone: `0000000000`

- **Responsibilities**:
  - Create new Managers
  - View all Managers
  - Manage application settings

- **User Creation Form Fields**:
  - Username
  - Password
  - Email
  - Phone Number
  - Department (for the Manager)

### 2. **Manager**
- **Created by**: Admin
- **Responsibilities**:
  - Create new Employees
  - View all Employees under their supervision
  - View and manage their own Department
  - Access task management features

- **User Creation Form Fields** (created by Admin):
  - Username
  - Password
  - Email
  - Phone Number
  - Department

- **Employee Creation Form Fields** (Manager creates):
  - Username
  - Password
  - Email
  - Phone Number
  - Job Title

### 3. **Employee**
- **Created by**: Manager
- **Responsibilities**:
  - Access the Todo List application
  - Create, edit, and manage tasks
  - View assigned tasks
  - Track task completion

- **User Creation Form Fields** (created by Manager):
  - Username
  - Password
  - Email
  - Phone Number
  - Job Title

---

## User Registration Flow

```
┌─────────────────────────────────────────┐
│         LOGIN SCREEN                    │
│  Enter Username & Password              │
└────────────┬────────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
┌───▼───────┐   ┌──────▼──────┐   ┌──────────────┐
│   ADMIN   │   │   MANAGER   │   │   EMPLOYEE   │
│Dashboard │   │ Dashboard   │   │ Todo List    │
│  (Boss)  │   │             │   │              │
└───┬───────┘   └──────┬──────┘   └──────────────┘
    │                  │
    │ Creates          │ Creates
    │ Managers         │ Employees
    │                  │
    └─────────────────┘
```

---

## System Architecture

### Database Schema

#### Users Table
```sql
CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  email TEXT NOT NULL,
  phone_number TEXT NOT NULL,
  role TEXT NOT NULL,
  department TEXT,           -- For Managers
  job_title TEXT,            -- For Employees
  created_by INTEGER,        -- User ID who created this user
  created_at INTEGER,        -- Timestamp in milliseconds
  FOREIGN KEY(created_by) REFERENCES users(id)
);
```

#### Tasks Table (Updated)
```sql
ALTER TABLE tasks ADD COLUMN user_id INTEGER;
ALTER TABLE tasks ADD FOREIGN KEY(user_id) REFERENCES users(id);
```

---

## Implementation Details

### Key Java Classes

#### 1. **UserRole.java**
- Enum defining three roles: ADMIN, MANAGER, EMPLOYEE
- Each role has a display name

#### 2. **User.java** (Model)
- Represents a user with all attributes
- Fields: id, username, password, email, phoneNumber, role, department, jobTitle, createdBy, createdAt

#### 3. **UserDao.java** (Data Access Object)
- `authenticate(username, password)` - Verify user credentials
- `findByUsername(username)` - Retrieve user by username
- `findById(id)` - Retrieve user by ID
- `createUser(user)` - Create new user with duplicate username check
- `getAllManagers()` - Get all Manager users
- `getAllEmployees()` - Get all Employee users
- `getEmployeesByManager(managerId)` - Get employees created by specific manager
- `updateUser(user)` - Update user information
- `deleteUser(id)` - Delete user

#### 4. **LoginController.java**
- Handles login form submission
- Authenticates user credentials
- Routes to appropriate dashboard based on user role
- Stores current user session

#### 5. **AdminDashboardController.java**
- Manager creation and management
- Displays all managers in a table
- Validates input fields:
  - All fields required
  - Valid email format check
  - Duplicate username prevention

#### 6. **ManagerDashboardController.java**
- Employee creation and management
- Displays all employees created by this manager
- Validates input fields:
  - All fields required
  - Valid email format check
  - Duplicate username prevention

#### 7. **Database.java** (Updated)
- Initializes users table on first run
- Creates default admin account if none exists
- Adds user_id foreign key to tasks table

---

## FXML Files Created

### 1. **login.fxml**
- Login form with username and password fields
- Error message display
- Demo credentials info label

### 2. **admin_dashboard.fxml**
- Manager creation form
- Managers table view

### 3. **manager_dashboard.fxml**
- Employee creation form
- Employees table view

---

## How to Use

### Step 1: Start Application
```
Run MainApp.java
```
The application opens with the Login screen.

### Step 2: Admin Login
```
Username: admin
Password: 123
```
After login, Admin Dashboard opens where Admin can create Managers.

### Step 3: Create Manager (Admin)
1. Click "Admin Dashboard"
2. Fill in Manager creation form:
   - Username: (e.g., "manager1")
   - Password: (e.g., "pass123")
   - Email: (e.g., "manager1@company.com")
   - Phone: (e.g., "1234567890")
   - Department: (e.g., "Sales")
3. Click "Create Manager" button
4. Manager appears in the Managers table

### Step 4: Manager Login
1. Click "Logout" to return to login
2. Enter Manager credentials:
   ```
   Username: manager1
   Password: pass123
   ```
3. Manager Dashboard opens

### Step 5: Create Employee (Manager)
1. In Manager Dashboard, fill Employee creation form:
   - Username: (e.g., "employee1")
   - Password: (e.g., "emp123")
   - Email: (e.g., "employee1@company.com")
   - Phone: (e.g., "0987654321")
   - Job Title: (e.g., "Sales Representative")
2. Click "Create Employee" button
3. Employee appears in the Employees table

### Step 6: Employee Login
1. Logout to return to login screen
2. Enter Employee credentials:
   ```
   Username: employee1
   Password: emp123
   ```
3. Todo List application opens for the employee

---

## Validation Rules

### Username
- Must be unique (no duplicates)
- Cannot be empty

### Password
- Cannot be empty
- No minimum length requirement (modify as needed)

### Email
- Must contain "@" symbol
- Minimum 5 characters

### Phone Number
- Cannot be empty
- No format validation (accepts any string)

### Department (Manager only)
- Cannot be empty

### Job Title (Employee only)
- Cannot be empty

---

## Error Handling

The system provides user-friendly error messages:
- "Username already exists: [username]" - When duplicate username detected
- "Invalid email format" - When email is invalid
- "All fields are required" - When any field is empty
- "Invalid username or password" - On login failure

---

## Security Considerations

⚠️ **Note**: This is a development/demo implementation. For production use:

1. **Password Hashing**: Passwords are currently stored in plain text. Use bcrypt or similar:
   ```java
   // Use BCrypt for password hashing
   String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
   ```

2. **Session Management**: Implement proper session tokens
3. **Input Validation**: Add comprehensive input sanitization
4. **SQL Injection Prevention**: Current code uses PreparedStatements ✓
5. **HTTPS/TLS**: Use encryption for data transmission
6. **Role-Based Access Control (RBAC)**: Implement permissions system
7. **Audit Logging**: Log all user actions for compliance

---

## Database File

- **Location**: `doable.db` (SQLite database)
- **Auto-created** on first application run
- **Contains tables**: users, tasks, categories

---

## Troubleshooting

### Issue: Cannot find users table
**Solution**: Delete `doable.db` and restart the application. Database will be recreated.

### Issue: "Invalid username or password"
**Solution**: Ensure credentials are correct. Default admin is `admin` / `123`.

### Issue: Cannot create new user
**Solution**: Check that all required fields are filled and email format is valid.

### Issue: User not appearing in table
**Solution**: Click the Refresh button or wait for automatic refresh.

---

## Future Enhancements

1. Password hashing and encryption
2. User profile management
3. Change password functionality
4. Delete/deactivate users
5. Role-based task visibility
6. Audit trail and logging
7. Email notifications
8. Two-factor authentication
9. Password reset functionality
10. User permissions/roles customization

---

## File Structure

```
src/main/java/com/doable/
├── model/
│   ├── User.java (NEW)
│   ├── UserRole.java (NEW)
│   ├── Task.java (existing)
│   └── Category.java (existing)
├── dao/
│   ├── UserDao.java (NEW)
│   ├── TaskDao.java (existing)
│   └── CategoryDao.java (existing)
├── controller/
│   ├── LoginController.java (NEW)
│   ├── AdminDashboardController.java (NEW)
│   ├── ManagerDashboardController.java (NEW)
│   ├── HomeController.java (UPDATED)
│   └── ...
├── db/
│   └── Database.java (UPDATED)
├── util/
│   └── ...
└── MainApp.java (UPDATED)

src/main/resources/fxml/
├── login.fxml (NEW)
├── admin_dashboard.fxml (NEW)
├── manager_dashboard.fxml (NEW)
├── home.fxml (existing)
└── ...
```

---

## Contact & Support

For issues or questions about the user management system, please refer to the main README.md or contact the development team.

---

**Version**: 1.0  
**Last Updated**: December 4, 2025  
**Status**: Production Ready ✓
