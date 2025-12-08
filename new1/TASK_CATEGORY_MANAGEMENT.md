# Task & Category Management with Role-Based Features

## Overview
The system now supports manager and admin task/category creation with full tracking of who created what, allowing differentiation between employee's own tasks and those created by managers.

## New Features Implemented

### 1. Password Editing for Employees
**File:** `ManagerDashboardController.java`
**Feature:** Managers can now edit employee passwords when editing employee information

#### How it Works:
- Click "Edit" on an employee or double-click to open edit dialog
- New "Password" field is optional (leave empty to keep current password)
- If password is entered, it updates; if left blank, current password is maintained
- Password field includes prompt: "Leave empty to keep current password"

#### Dialog Fields:
- Username
- **Password** (NEW - optional)
- Email
- Phone
- Job Title

---

### 2. Task Management System

#### Task Creation by Manager
**File:** `ManagerDashboardController.java`
**Method:** `createTask(String title, String description, long employeeId)`

- Manager can create tasks for employees
- Tasks are tracked with:
  - `userId`: The employee who owns/receives the task
  - `createdBy`: The manager who created it (currentUser.getId())
  - `created_at`: Timestamp of creation

#### Task Creation by Admin/Boss
**File:** `AdminDashboardController.java`
**Method:** `createTask(String title, String description, long userId)`

- Admin can create tasks for any user
- Tasks are tracked with:
  - `userId`: The user who owns the task
  - `createdBy`: The admin who created it

#### Task Model Updates
**File:** `Task.java`

New fields added:
```java
private long userId;              // Task owner
private long createdBy;           // Who created this task
private String createdByUsername; // Username of creator
```

New Getters/Setters:
- `getUserId()` / `setUserId()`
- `getCreatedBy()` / `setCreatedBy()`
- `getCreatedByUsername()` / `setCreatedByUsername()`

#### Database Schema
**File:** `Database.java`

Tasks table updated with:
```sql
CREATE TABLE tasks (
    ...existing columns...
    user_id INTEGER,              -- Task owner
    created_by INTEGER,           -- Who created the task
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(created_by) REFERENCES users(id)
)
```

#### TaskDao Updates
**File:** `TaskDao.java`

Methods updated to handle user_id and created_by:
- `insert()`: Now saves user_id and created_by
- `update()`: Now updates user_id and created_by

---

### 3. Category Management System

#### Category Creation by Manager
**File:** `ManagerDashboardController.java`
**Method:** `createCategory(String name, String color)`

- Manager can create categories
- Categories are tracked with:
  - `createdBy`: The manager who created it (currentUser.getId())

#### Category Creation by Admin/Boss
**File:** `AdminDashboardController.java`
**Method:** `createCategory(String name, String color)`

- Admin can create categories for system-wide use
- Categories are tracked with:
  - `createdBy`: The admin who created it

#### Category Model Updates
**File:** `Category.java`

New fields added:
```java
private long createdBy;           // Who created this category
private String createdByUsername; // Username of creator
```

New Getters/Setters:
- `getCreatedBy()` / `setCreatedBy()`
- `getCreatedByUsername()` / `setCreatedByUsername()`

#### Database Schema
**File:** `Database.java`

Categories table updated with:
```sql
CREATE TABLE categories (
    ...existing columns...
    created_by INTEGER,
    FOREIGN KEY(created_by) REFERENCES users(id)
)
```

#### CategoryDao Updates
**File:** `CategoryDao.java`

Methods updated to handle created_by:
- `insert()`: Now saves created_by
- `update()`: Now updates created_by
- `findById()`: Now retrieves created_by
- `findAll()`: Now retrieves created_by for all categories

---

## Differentiation Strategy

### For Tasks
**Filtering Logic:**
1. **Employee's Own Tasks**: Tasks where `user_id = employee_id` AND `created_by = null/employee_id`
2. **Manager-Created Tasks**: Tasks where `user_id = employee_id` AND `created_by = manager_id`
3. **Admin-Created Tasks**: Tasks where `user_id = employee_id` AND `created_by = admin_id`

### For Categories
**Display Information:**
- Show creator information alongside category
- Format: "Category Name (created by: username)"
- Admin categories: "Category Name (created by: admin)"
- Manager categories: "Category Name (created by: manager_name)"

**UI Display:**
When displaying tasks or categories in employee view:
```
Regular Task
   Created by Employee

Manager Task
   ⭐ Created by Manager: manager_username

Admin Task
   ⭐ Created by Admin: admin_username
```

---

## Database Diagram

```
Users Table
├── id (PK)
├── username
├── password
├── email
├── phone_number
├── role
├── department
├── job_title
├── created_by (FK → Users)
└── created_at

Tasks Table (UPDATED)
├── id (PK)
├── title
├── description
├── due
├── completed
├── repeat_rule
├── category_id (FK → Categories)
├── user_id (FK → Users) ← Task owner
├── created_by (FK → Users) ← Who created it (NEW)
└── marked_for_completion

Categories Table (UPDATED)
├── id (PK)
├── name
├── color
└── created_by (FK → Users) ← Who created it (NEW)
```

---

## Implementation Details

### Migration Path
For existing data:
- `created_by` field defaults to NULL for old tasks/categories
- When NULL, treat as employee's own creation or system default
- New items will always have created_by set

### Data Integrity
- Foreign keys ensure created_by references valid users
- Deletion cascade policies:
  - When user is deleted, set created_by to NULL (or cascade delete based on policy)
  - Tasks/categories with deleted creator remain but show creator as "Deleted User"

### Query Examples

**Get all tasks assigned to employee by a specific manager:**
```sql
SELECT * FROM tasks 
WHERE user_id = ? 
AND created_by = (SELECT id FROM users WHERE username = ?)
```

**Get all categories created by current manager:**
```sql
SELECT * FROM categories 
WHERE created_by = ?
```

**Differentiate task sources:**
```sql
-- Employee's own tasks
SELECT * FROM tasks WHERE user_id = ? AND (created_by IS NULL OR created_by = ?)

-- Manager-assigned tasks
SELECT * FROM tasks 
WHERE user_id = ? 
AND created_by IN (SELECT id FROM users WHERE role = 'MANAGER')

-- Admin-assigned tasks
SELECT * FROM tasks 
WHERE user_id = ? 
AND created_by IN (SELECT id FROM users WHERE role = 'ADMIN')
```

---

## Testing Checklist

- [x] Code compiles without errors
- [ ] Manager can edit employee password (manual test)
- [ ] Manager can create tasks with `createTask()` method
- [ ] Manager can create categories with `createCategory()` method
- [ ] Admin can create tasks and categories
- [ ] created_by field properly populated for all new tasks
- [ ] created_by field properly populated for all new categories
- [ ] Database migration handles new columns correctly
- [ ] Existing data without created_by still works (NULL handling)
- [ ] UI displays task source information (when implemented in views)
- [ ] Filter logic correctly identifies owner vs assigned tasks

---

## Usage Examples

### For Manager Creating Tasks
```java
// In ManagerDashboardController
ManagerDashboardController controller = // get controller instance
controller.createTask("Complete report", "Finish the monthly report", employeeId);
```

### For Admin Creating Tasks
```java
// In AdminDashboardController
AdminDashboardController controller = // get controller instance
controller.createTask("Project kickoff", "Start new project X", userId);
```

### For Manager Creating Categories
```java
// In ManagerDashboardController
controller.createCategory("Urgent", "#FF0000");
controller.createCategory("Routine", "#00FF00");
```

### For Admin Creating Categories
```java
// In AdminDashboardController
controller.createCategory("Work", "#0000FF");
controller.createCategory("Personal", "#FFFF00");
```

---

## Future Enhancements

1. **UI for Task/Category Creation**: Add forms in dashboard to allow managers/admins to create tasks and categories directly
2. **Task Filtering**: Add filters to show "My Tasks" vs "Assigned Tasks"
3. **Creator Display**: Show creator name on tasks/categories in employee view
4. **Permission Control**: Manager can only create tasks for their employees, not for all users
5. **Notification System**: Notify employees when manager assigns a task
6. **Task Delegation**: Allow reassigning tasks between managers

---

## Build Status
✅ **COMPILATION SUCCESSFUL** - All changes compile without errors

## Files Modified
1. ✅ Task.java - Added userId, createdBy, createdByUsername
2. ✅ Category.java - Added createdBy, createdByUsername
3. ✅ Database.java - Updated schema for tasks and categories tables
4. ✅ TaskDao.java - Updated insert/update for new fields
5. ✅ CategoryDao.java - Updated insert/update/findById/findAll for created_by
6. ✅ ManagerDashboardController.java - Added password editing, createTask, createCategory methods
7. ✅ AdminDashboardController.java - Added createTask, createCategory methods

## Next Steps
1. Run application with `mvn javafx:run`
2. Test manager editing employee password
3. Implement UI forms for creating tasks and categories
4. Implement filtering to show task source
5. Test differentiation between employee's own tasks and assigned tasks
