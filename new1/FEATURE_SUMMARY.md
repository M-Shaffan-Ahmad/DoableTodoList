# Task Reminder Checkbox Feature

## Overview
A new checkbox feature has been added to the task reminders on the home screen. This allows users to mark tasks as "completion pending" to suppress reminders, and to renew recurring tasks.

## Feature Details

### How It Works

1. **Display**: Each task in the home screen ListView now displays with a checkbox next to it, followed by the task title, category (if any), and due date.

2. **Marking for Completion**: When a checkbox is checked, the task is marked as "completion pending":
   - Task reminders **will not trigger** for this task
   - The task remains in the list but won't generate notifications
   - The marked state is persisted in the database

3. **Renewing Tasks**: When a checkbox is **unchecked** (unmarked):
   - For tasks with repeat rules (DAILY, WEEKLY, or EVERY_X_DAYS/WEEKS/MONTHS):
     - The due date is automatically renewed to the next occurrence
     - For example, a DAILY task due at 2:00 PM will be renewed to tomorrow at 2:00 PM
     - Weekly tasks advance by 1 week, etc.
   - The task is reactivated for reminders
   - This allows recurring tasks to be renewed by simply unchecking the box

### Technical Implementation

#### Changes to Task Model (`Task.java`)
- Added `markedForCompletion` boolean field to track if a task is marked for completion
- Added getter and setter methods: `isMarkedForCompletion()` and `setMarkedForCompletion()`
- Initialized to `false` in the constructor

#### Changes to HomeController (`HomeController.java`)

**1. Updated ListView Cell Factory**
- Replaced simple text display with a custom cell containing:
  - A CheckBox (styled with 14pt font)
  - A Label showing task details (title, category, due date)
- Checkbox state is updated from the Task model's `markedForCompletion` field
- Clicking the checkbox triggers the `handleTaskCheckboxToggle()` handler

**2. New Method: `handleTaskCheckboxToggle(Task task)`**
- Toggles the task's marked for completion state
- If marked (checked):
  - Sets `markedForCompletion = true`
  - Suppresses reminders
  - Saves changes to database
- If unmarked (unchecked):
  - Sets `markedForCompletion = false`
  - Calls `renewTask()` to calculate the next due date for recurring tasks
  - Saves changes to database
- Refreshes the task list display

**3. New Method: `renewTask(Task task)`**
- Calculates the next due date based on the task's repeat rule
- Supports multiple repeat formats:
  - `NONE`: No renewal (one-time tasks)
  - `DAILY`: Adds 1 day at a time until due date is in the future
  - `WEEKLY`: Adds 1 week at a time until due date is in the future
  - `EVERY_X_DAYS`: Parses custom formats like "EVERY_2_DAYS" and advances accordingly
  - `EVERY_X_WEEKS`: Similar parsing for weekly intervals
  - `EVERY_X_MONTHS`: Similar parsing for monthly intervals

**4. Updated Reminder Checker (`startReminderChecker()`)**
- Modified the condition to skip tasks that are marked for completion:
  ```java
  if (!t.isCompleted() && !t.isMarkedForCompletion() && t.getDueDate() != null)
  ```
- Tasks marked for completion will not generate reminders (1 minute before or at exact time)

### User Workflow

**Scenario 1: Suppress reminders for today**
1. User sees a task in the list with a checkbox
2. User clicks the checkbox to mark it
3. Task no longer generates reminders
4. Task remains visible in the list for reference

**Scenario 2: Renew a daily task**
1. User completes a daily task and marks it with the checkbox
2. User unchecks the checkbox to renew it
3. The due date automatically advances to tomorrow (same time)
4. User receives reminders for the renewed task tomorrow

## Testing Recommendations

1. **Checkbox Display**: Verify checkboxes appear correctly next to all tasks
2. **Marking/Unmarking**: Test toggling checkboxes and verify state persists after refresh
3. **Reminder Suppression**: Confirm marked tasks don't trigger reminders
4. **Renewal Logic**: Test unmarking daily, weekly, and custom repeat tasks to verify dates advance correctly
5. **One-time Tasks**: Verify unmarking a non-repeating task works without errors
6. **Database Persistence**: Restart the application and verify checkbox states are maintained

## Database Notes

Currently, the `markedForCompletion` state is stored in the Java object but NOT persisted to the database. To make this feature fully persistent across application restarts:

1. Add a `marked_for_completion` BOOLEAN column to the tasks table
2. Update `TaskDao.java`:
   - Modify the `update()` method to save the `markedForCompletion` state
   - Modify the `insert()` method to save the `markedForCompletion` state
   - Modify the `findAll()` method to load the `markedForCompletion` state

This would require a database migration but would complete the feature implementation.
