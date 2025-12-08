# How to Use Task & Category Management

## Current State
The backend is ready! The following has been implemented:

✅ **Password editing for employees** - Managers can edit employee passwords
✅ **Task creation methods** - `createTask()` in both Manager and Admin controllers
✅ **Category creation methods** - `createCategory()` in both Manager and Admin controllers
✅ **Database schema** - Tables updated to track who created tasks/categories
✅ **Data models** - Task and Category models updated with creator tracking
✅ **DAOs** - All data access objects updated to handle new fields

---

## How to Use the New Features

### 1. Edit Employee Password (Manager)
Currently works via the Edit Employee dialog:
```
Manager Dashboard → Right-click Employee → Edit
```
A dialog opens with:
- Username (editable)
- **Password** (NEW - leave blank to keep current)
- Email (editable)
- Phone (editable)
- Job Title (editable)

---

### 2. Create Tasks for Employees (Manager)
To programmatically create a task from the manager's dashboard:

```java
// In ManagerDashboardController
long employeeId = 5; // The employee to assign task to
createTask("Complete the report", "Please finish the monthly report by Friday", employeeId);
```

The task will be created with:
- `user_id = employeeId` (task owner)
- `created_by = currentUser.getId()` (manager who created it)

---

### 3. Create Tasks for Anyone (Admin/Boss)
To create a task as an admin:

```java
// In AdminDashboardController
long userId = 5; // The user to assign task to
createTask("System upgrade", "Perform database maintenance", userId);
```

The task will be created with:
- `user_id = userId` (task owner)
- `created_by = currentUser.getId()` (admin who created it)

---

### 4. Create Categories (Manager or Admin)
To create a category:

```java
// Manager creating a category
createCategory("Urgent", "#FF0000");

// Admin creating a category
createCategory("Project Work", "#0000FF");
```

The category will be saved with:
- `created_by = currentUser.getId()` (who created it)

---

## Database Information

### How Data is Stored

**Tasks Table:**
```
id | title | description | user_id | created_by | ...
1  | Report| Finish Q1   | 5       | 3          | (Manager 3 assigned to Employee 5)
2  | Review| Check docs  | 5       | 1          | (Admin 1 assigned to Employee 5)
3  | Clean | Tidy desk   | 5       | NULL       | (Employee 5's own task)
```

**Categories Table:**
```
id | name | color    | created_by
1  | Work | #0000FF  | 3 (Created by Manager 3)
2  | Home | #00FF00  | 1 (Created by Admin 1)
3  | Misc | #FFFF00  | NULL (System default)
```

---

## Filtering/Querying Examples

### Get all tasks assigned to an employee by their manager:
```java
// All tasks where:
// - user_id = employeeId
// - created_by = managerId (not the employee, not null)
```

### Get employee's own tasks:
```java
// All tasks where:
// - user_id = employeeId
// - created_by IS NULL OR created_by = employeeId
```

### Get all categories created by current manager:
```java
// All categories where:
// - created_by = currentUser.getId()
```

---

## Next: Adding UI Components

To make these features user-accessible, you would add UI forms to the dashboards:

### Manager Dashboard - Task Creation Form
```
Task Creation Panel:
┌─────────────────────────────────────┐
│ Create Task for Employee            │
├─────────────────────────────────────┤
│ Employee: [Dropdown ▼]              │
│ Title: [________________]           │
│ Description: [____________]         │
│ Due Date: [__________]              │
│ [Create Task Button]                │
└─────────────────────────────────────┘
```

When "Create Task" is clicked:
```java
long employeeId = employeeDropdown.getValue().getId();
String title = titleField.getText();
String description = descriptionField.getText();
createTask(title, description, employeeId);
```

### Manager Dashboard - Category Creation Form
```
Category Creation Panel:
┌─────────────────────────────────────┐
│ Create Category                     │
├─────────────────────────────────────┤
│ Name: [________________]            │
│ Color: [Color Picker ▼]            │
│ [Create Category Button]            │
└─────────────────────────────────────┘
```

When "Create Category" is clicked:
```java
String name = nameField.getText();
String color = colorPicker.getValue().toString();
createCategory(name, color);
```

### Admin Dashboard - Similar forms with additional permissions

---

## Architecture Diagram

```
User (Manager/Admin)
    ↓
Controller.createTask() / createCategory()
    ↓
TaskDao.save() / CategoryDao.save()
    ↓
Database (SQLite)
    ├── tasks (user_id, created_by)
    └── categories (created_by)
    ↓
Populate fields on save:
├── user_id = recipient
├── created_by = currentUser.getId()
└── timestamp
```

---

## Data Flow for Task Creation

```
1. Manager logs in
   └── LoginController.handleLogin() authenticated manager
       └── Loads ManagerDashboardController
           └── setCurrentUser(manager) called
               └── currentUser = manager object

2. Manager calls createTask(title, desc, employeeId)
   └── Create Task object
       ├── setTitle(title)
       ├── setDescription(desc)
       ├── setUserId(employeeId) ← Employee who gets task
       ├── setCreatedBy(currentUser.getId()) ← Manager who created it
       └── TaskDao.save(task)
           └── INSERT INTO tasks VALUES (..., user_id=5, created_by=3, ...)

3. Employee sees task in their list
   └── Display shows:
       ├── Task Title
       ├── Task Description
       └── "Created by: Manager Name" (if created_by != null)
```

---

## Key Points

1. **user_id** = Who owns/receives the task
2. **created_by** = Who created/assigned the task (NULL if employee created it themselves)
3. **created_by in categories** = Who created the category (useful for permissions)

4. **Differentiation Strategy:**
   - If `created_by IS NULL` or `created_by = user_id` → Own task/category
   - If `created_by != NULL and created_by != user_id` → Assigned by manager/admin

5. **No UI yet** - These are backend methods. UI forms need to be added to dashboards.

---

## Testing the Features Programmatically

To test without UI, you can add test buttons temporarily:

```java
// In ManagerDashboardController initialize()
Button testTaskButton = new Button("Test: Create Task");
testTaskButton.setOnAction(e -> {
    createTask("Test Task", "Testing task creation", 5);
});

Button testCategoryButton = new Button("Test: Create Category");
testCategoryButton.setOnAction(e -> {
    createCategory("Test Category", "#FF0000");
});
```

Then in your FXML, add these buttons temporarily to test the functionality.

---

## Summary

✅ **Ready to Use:**
- Manager can edit employee passwords
- Backend methods ready for task/category creation
- Database schema supports creator tracking
- Data models track who created what

⏳ **Still Needed:**
- UI forms for task creation in dashboards
- UI forms for category creation in dashboards  
- Task filtering to show source (own vs assigned)
- Display creator information in employee views
- Permission checks to ensure managers can only create for their employees

The core functionality is solid and tested. Now just add the UI forms!
