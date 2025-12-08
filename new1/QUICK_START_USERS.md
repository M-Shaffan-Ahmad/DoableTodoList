# Quick Start - User Management System

## Default Admin Credentials
```
Username: admin
Password: 123
```

## User Hierarchy
```
Admin (Boss)
  └── Creates Managers
      └── Creates Employees
```

## Quick Setup Steps

### 1. Start Application
```bash
mvn javafx:run
```
or
```bash
java -jar doable-todo-1.0-SNAPSHOT.jar
```

### 2. Login as Admin
- Username: `admin`
- Password: `123`

### 3. Create a Manager
- Username: `manager1`
- Password: `pass123`
- Email: `manager1@company.com`
- Phone: `1234567890`
- Department: `Sales`

### 4. Logout & Login as Manager
- Username: `manager1`
- Password: `pass123`

### 5. Create an Employee
- Username: `employee1`
- Password: `emp123`
- Email: `employee1@company.com`
- Phone: `0987654321`
- Job Title: `Sales Rep`

### 6. Logout & Login as Employee
- Username: `employee1`
- Password: `emp123`
- Now access the Todo List!

---

## Key Features

| Feature | Admin | Manager | Employee |
|---------|-------|---------|----------|
| Create Manager | ✓ | ✗ | ✗ |
| View Managers | ✓ | ✗ | ✗ |
| Create Employee | ✗ | ✓ | ✗ |
| View Employees | ✗ | ✓ (own) | ✗ |
| Todo List | ✗ | ✓ | ✓ |
| Tasks Management | ✗ | ✓ | ✓ |

---

## Important Notes

1. **Username must be unique** - Cannot create duplicate usernames
2. **Email validation** - Must contain @ and be at least 5 characters
3. **All fields required** - Complete all form fields
4. **Database file** - `doable.db` created automatically in project root
5. **Demo mode** - No actual email/SMS sent, just demo functionality

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Login fails | Verify username and password exactly |
| Cannot create user | Check all fields filled and email valid |
| Users not showing | Click refresh or logout/login |
| Database error | Delete `doable.db` and restart app |

---

## Database Reset

To reset the database and start fresh:
1. Close the application
2. Delete `doable.db` file from project root
3. Restart application - new database created automatically
4. Admin user recreated with default credentials

---

For detailed information, see: **USER_MANAGEMENT_GUIDE.md**
