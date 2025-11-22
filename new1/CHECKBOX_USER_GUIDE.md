# Task Reminder Checkbox Feature - User Guide

## Visual Example

### Home Screen Display

```
┌─────────────────────────────────────────────────────────────────┐
│                       Doable - Todo List                       │
├─────────────────────────────────────────────────────────────────┤
│ Today                                                           │
│ Filter: [All  ▼]  Category: [All Categories ▼]              │
│                             [⚙ Settings]  [+ Add Task]       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ☑ Buy groceries [Shopping] — 2025-11-22 14:00:00            │
│                                                                 │
│  ☐ Finish project report [Work] — 2025-11-22 17:30:00        │
│                                                                 │
│  ☑ Call mom [Personal] — 2025-11-23 10:00:00                 │
│                                                                 │
│  ☐ Exercise [Health] — 2025-11-22 18:00:00                   │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│  [Edit]  [Delete]  [Refresh]        Showing 4 of 4 tasks      │
└─────────────────────────────────────────────────────────────────┘
```

- **☑** = Task marked for completion (checkbox checked)
- **☐** = Task active and ready (checkbox unchecked)

## Usage Scenarios

### Scenario 1: Daily Task Workflow

**Monday, 2:00 PM - Initial State**
- Task: "Morning jog" (DAILY repeat)
- Due: Monday 8:00 AM
- Status: ☐ (unchecked, reminders active)
- Result: Reminder showed at 7:59 AM

**Monday, 2:00 PM - User marks task complete**
- User clicks the checkbox: ☐ → ☑
- Task is marked for completion
- Result: No more reminders for today

**Tuesday, 8:00 AM - User wants to renew**
- User sees the task still marked: ☑
- User unchecks the checkbox: ☑ → ☐
- System automatically renews the task:
  - Old due date: Monday 8:00 AM
  - New due date: Tuesday 8:00 AM (advances by 1 day)
- Result: Reminders active again for Tuesday

---

### Scenario 2: Weekly Task Workflow

**Week 1 - Task Setup**
- Task: "Team meeting" (WEEKLY repeat)
- Due: Friday 10:00 AM
- Status: ☐ (active)

**After meeting on Friday**
- User marks task: ☐ → ☑
- Result: No more reminders for this week

**Next Tuesday**
- User realizes they need the reminder
- User unchecks: ☑ → ☐
- System renews:
  - Old due date: Friday (last week) 10:00 AM
  - New due date: Friday (next week) 10:00 AM
- Result: Reminder will trigger next Friday at 9:59 AM

---

### Scenario 3: Custom Repeat Interval

**Setup**
- Task: "Dentist appointment" (EVERY_3_WEEKS repeat)
- Due: Monday 9:00 AM
- Status: ☐

**After 3 weeks on Monday**
- User marks: ☐ → ☑
- Suppresses reminders

**User changes mind (Wednesday)**
- User unchecks: ☑ → ☐
- System renews:
  - Old due date: Monday 9:00 AM
  - New due date: Monday (3 weeks later) 9:00 AM
- Result: Reminders active for the next appointment

---

### Scenario 4: One-Time Task

**Task Setup**
- Task: "Pay bills" (NONE repeat)
- Due: December 1, 2025 3:00 PM
- Status: ☐

**User marks complete**
- User clicks: ☐ → ☑
- Result: Reminders suppressed

**User unchecks**
- User clicks: ☑ → ☐
- System attempts renewal but finds NONE repeat rule
- Result: Due date stays December 1, 2025 3:00 PM
- Result: Reminders reactivate for the same date

---

## Reminder Behavior

### When Checkbox is UNCHECKED (☐)
- Reminders are **ACTIVE**
- Notification triggers 1 minute before due date
- Notification triggers at exact due time
- Recurring tasks get renewed when unchecked

### When Checkbox is CHECKED (☑)
- Reminders are **SUPPRESSED**
- No notifications will be generated
- Task remains visible in list
- Uncheck to renew and reactivate reminders

---

## Key Benefits

1. **Flexible Reminder Management**: Suppress reminders without deleting the task
2. **Easy Task Renewal**: One click to renew recurring tasks
3. **Non-Destructive**: Checked tasks can be easily unchecked
4. **Visual Feedback**: Checkboxes provide clear status indication
5. **Persistent State**: Checkbox states are remembered after refresh (once DB persistence is added)

---

## Implementation Details

### Code Flow: Checkbox Toggle

```
User clicks checkbox
    ↓
handleTaskCheckboxToggle(task) called
    ↓
Toggle markedForCompletion flag
    ↓
  IF marked (checked):
    └─ Suppress reminders
  IF unmarked (unchecked):
    └─ renewTask(task) called
       ├─ Check repeat rule
       ├─ Calculate next due date
       └─ Update task
    ↓
taskDao.save(task)
    ↓
loadTasks() refreshes display
    ↓
ListView updates checkbox state
```

### Reminder Checker Logic

```
Every 1 second:
  For each task in database:
    IF task is not completed AND
       task is NOT marked for completion AND
       task has a due date:
      
      → Check if reminder should trigger
      → 1 minute before: Send notification
      → Exact time: Send notification
```

---

## Future Enhancement: Database Persistence

**Current Status**: Checkbox state is stored in memory only

**To Add Full Persistence**:
1. Add column to database: `ALTER TABLE tasks ADD COLUMN marked_for_completion BOOLEAN DEFAULT 0`
2. Update TaskDao insert/update methods to handle the new field
3. Update findAll() to load the marked_for_completion value from database
4. Checkbox states will then survive application restarts

This would require a database schema migration but would complete the feature implementation.
