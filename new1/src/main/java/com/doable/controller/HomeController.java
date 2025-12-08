package com.doable.controller;

import com.doable.util.NotificationUtil;
import com.doable.dao.CategoryDao;
import com.doable.dao.UserDao;
import com.doable.model.Category;
import com.doable.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.doable.dao.TaskDao;
import com.doable.model.Task;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HomeController {
    @FXML private ListView<Task> taskList;
    @FXML private ListView<Task> completedTaskList;
    @FXML private ChoiceBox<String> filterChoice;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private ChoiceBox<String> taskTypeFilter;
    @FXML private DatePicker completedDateFilter;
    @FXML private ComboBox<String> completedCategoryFilter;
    @FXML private Label statusLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private final TaskDao taskDao = new TaskDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Task> completedTasks = FXCollections.observableArrayList();
    private List<Task> allTasks = List.of();
    private List<Task> allCompletedManagerTasks = List.of();
    private final Set<Long> notifiedTaskIds = new HashSet<>(); // Track which tasks have been notified
    private final Set<Long> notifiedAtExactTimeIds = new HashSet<>(); // Track tasks notified at exact due time
    
    private User currentUser;

    public void initialize() {
        filterChoice.getItems().addAll("All", "Pending", "Completed");
        filterChoice.setValue("All");
        
        taskTypeFilter.getItems().addAll("All Tasks", "Manager Assigned", "My Tasks");
        taskTypeFilter.setValue("All Tasks");
        
        taskList.setItems(tasks);
        taskList.setCellFactory(lv -> new ListCell<>() {
            private final Label checkBoxLabel = new Label();
            private final Label taskLabel = new Label();

            {
                checkBoxLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-min-width: 25;");
                taskLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #333333;");
                taskLabel.setWrapText(true);
                taskLabel.setMaxWidth(600);
            }

            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Display visual checkbox: ✓ for marked, ☐ for unmarked
                    if (item.isMarkedForCompletion()) {
                        checkBoxLabel.setText("✓");
                        checkBoxLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-min-width: 25; -fx-text-fill: #10b981;");
                    } else {
                        checkBoxLabel.setText("☐");
                        checkBoxLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-min-width: 25; -fx-text-fill: #6b7280;");
                    }
                    
                    // Make label clickable
                    checkBoxLabel.setCursor(javafx.scene.Cursor.HAND);
                    checkBoxLabel.setOnMouseClicked(e -> handleTaskCheckboxToggle(item));

                    // Build task display text
                    String display = item.getTitle();
                    if (item.getCategoryName() != null && !item.getCategoryName().isEmpty()) {
                        display += " [" + item.getCategoryName() + "]";
                    }
                    if (item.getDueDate() != null) {
                        display += " — " + item.getDueDate();
                    }
                    
                    // Add manager badge if assigned by manager
                    if ("MANAGER".equals(item.getAssignmentType())) {
                        display = "👔 " + display; // Add manager badge
                    }
                    
                    taskLabel.setText(display);

                    // Create HBox with checkbox and task label
                    HBox hbox = new HBox(10);
                    hbox.setStyle("-fx-padding: 5;");
                    
                    // Highlight manager-assigned tasks with prominent yellow background and styling
                    if ("MANAGER".equals(item.getAssignmentType())) {
                        hbox.setStyle("-fx-padding: 8; -fx-background-color: #fef08a; -fx-border-color: #f59e0b; -fx-border-width: 2; -fx-border-radius: 4;");
                        taskLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #1f2937; -fx-font-weight: bold;");
                    } else {
                        taskLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #333333;");
                    }
                    
                    hbox.getChildren().addAll(checkBoxLabel, taskLabel);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
        
        // Load categories
        loadCategories();
        
        // Add filter listeners
        filterChoice.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> applyFilters());
        categoryFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> applyFilters());
        taskTypeFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> applyFilters());
        
        // Setup completed tasks list view
        completedTaskList.setItems(completedTasks);
        completedTaskList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label taskLabel = new Label();
                    String display = "✓ " + item.getTitle();
                    if (item.getCategoryName() != null && !item.getCategoryName().isEmpty()) {
                        display += " [" + item.getCategoryName() + "]";
                    }
                    if (item.getDueDate() != null) {
                        display += " — " + item.getDueDate();
                    }
                    taskLabel.setText(display);
                    taskLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #10b981;");
                    taskLabel.setWrapText(true);
                    taskLabel.setMaxWidth(600);
                    HBox hbox = new HBox(10, taskLabel);
                    hbox.setStyle("-fx-padding: 5; -fx-background-color: #f0fdf4; -fx-border-color: #10b981; -fx-border-width: 1;");
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
        
        // Setup completed tasks filters
        completedDateFilter.setOnAction(e -> applyCompletedFilters());
        completedCategoryFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> applyCompletedFilters());
        
        loadTasks();
        loadCompletedManagerTasks();
        startReminderChecker();
    }
    
    private void loadCategories() {
        try {
            categoryFilter.getItems().clear();
            // Add "All Categories" option
            Category allCat = new Category(0, "All Categories");
            categoryFilter.getItems().add(allCat);
            categoryFilter.getItems().addAll(categoryDao.findAll());
            categoryFilter.setValue(allCat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        try {
            if (currentUser == null) {
                allTasks = List.of();
            } else {
                // Load tasks assigned to this employee via assignments table
                com.doable.dao.AssignmentDao assignmentDao = new com.doable.dao.AssignmentDao();
                List<com.doable.model.Assignment> assignments = assignmentDao.findByEmployeeId(currentUser.getId());
                
                List<Task> tasks = new java.util.ArrayList<>();
                for (com.doable.model.Assignment assignment : assignments) {
                    if (!assignment.isMarkedForCompletion()) {  // Only load pending assignments
                        Task task = taskDao.findById(assignment.getTaskId());
                        if (task != null) {
                            // Set marked_for_completion from assignment, not from task
                            task.setMarkedForCompletion(assignment.isMarkedForCompletion());
                            
                            // Determine if task was assigned by manager or self-created
                            // If assignedBy is different from current user, it's a manager-assigned task
                            if (assignment.getAssignedBy() != currentUser.getId()) {
                                task.setAssignmentType("MANAGER");
                            } else {
                                task.setAssignmentType("PERSONAL");
                            }
                            
                            tasks.add(task);
                        }
                    }
                }
                allTasks = tasks;
                System.out.println("DEBUG: Loaded " + allTasks.size() + " pending tasks for employee: " + currentUser.getUsername());
            }
            applyFilters();
        } catch (SQLException e) {
            statusLabel.setText("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void applyFilters() {
        String statusFilter = filterChoice.getValue();
        String taskTypeFilter = this.taskTypeFilter.getValue();
        Category selectedCategory = categoryFilter.getValue();
        LocalDateTime now = LocalDateTime.now();
        
        List<Task> filtered = allTasks.stream()
            .filter(t -> {
                // Apply task type filter
                if ("Manager Assigned".equals(taskTypeFilter)) {
                    return "MANAGER".equals(t.getAssignmentType());
                } else if ("My Tasks".equals(taskTypeFilter)) {
                    return !"MANAGER".equals(t.getAssignmentType());
                }
                return true; // "All Tasks"
            })
            .filter(t -> {
                // Apply status filter
                if ("Pending".equals(statusFilter)) {
                    // Pending: not completed AND due date hasn't passed yet
                    if (t.isCompleted()) {
                        return false; // Already completed by user or system
                    }
                    if (t.getDueDate() != null && t.getDueDate().isBefore(now)) {
                        return false; // Due date has passed
                    }
                    return true; // Not completed and future due date
                } else if ("Completed".equals(statusFilter)) {
                    // Completed: either marked as completed by user OR due date has passed
                    if (t.isCompleted()) {
                        return true; // User marked as completed
                    }
                    if (t.getDueDate() != null && t.getDueDate().isBefore(now)) {
                        return true; // Due date has passed
                    }
                    return false; // Still pending
                }
                return true; // "All" - include all tasks
            })
            .filter(t -> {
                // Apply category filter
                if (selectedCategory == null || selectedCategory.getId() == 0) {
                    return true; // "All Categories" - include all tasks
                }
                return t.getCategoryId() == selectedCategory.getId();
            })
            .sorted((t1, t2) -> {
                // Sort: manager-assigned tasks first, then by incomplete/complete
                boolean t1IsManager = "MANAGER".equals(t1.getAssignmentType());
                boolean t2IsManager = "MANAGER".equals(t2.getAssignmentType());
                
                if (t1IsManager != t2IsManager) {
                    return Boolean.compare(t2IsManager, t1IsManager); // Manager tasks first
                }
                
                // Then sort: incomplete (markedForCompletion=false) first, then completed (markedForCompletion=true)
                if (t1.isMarkedForCompletion() == t2.isMarkedForCompletion()) {
                    // If same marked status, maintain original order by due date
                    if (t1.getDueDate() != null && t2.getDueDate() != null) {
                        return t1.getDueDate().compareTo(t2.getDueDate());
                    }
                    return 0;
                }
                // false (unmarked/incomplete) comes before true (marked/complete)
                return Boolean.compare(t1.isMarkedForCompletion(), t2.isMarkedForCompletion());
            })
            .toList();
        
        tasks.setAll(filtered);
        updateEditDeleteButtonStatus();
        statusLabel.setText("Showing " + filtered.size() + " of " + allTasks.size() + " tasks");
    }

    @FXML
    private void onSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("/fxml/settings.fxml"));
            Parent root = loader.load();
            SettingsController ctrl = loader.getController();

            Stage s = new Stage();
            s.initModality(Modality.APPLICATION_MODAL);
            s.setTitle("Settings");
            s.setScene(new Scene(root, 600, 700));
            ctrl.setStage(s);
            s.showAndWait();

            // Refresh tasks and categories when settings window closes
            loadTasks();
            loadCategories();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException loading settings: " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                cause = cause.getCause();
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open settings");
            alert.setContentText(e.getMessage() + (e.getCause() != null ? "\n" + e.getCause().getMessage() : ""));
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception loading settings: " + e.getClass().getName() + ": " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open settings");
            alert.setContentText(e.getClass().getName() + ": " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onAddTask() {
        openEditor(null);
    }

    @FXML
    private void onEditTask() {
        Task sel = taskList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Please select a task to edit.");
            a.showAndWait();
            return;
        }
        
        // Prevent editing manager-assigned tasks
        if ("MANAGER".equals(sel.getAssignmentType())) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Manager-assigned tasks cannot be edited. You can only mark them as complete.");
            a.showAndWait();
            return;
        }
        
        openEditor(sel);
    }

    @FXML
    private void onDeleteTask() {
        Task sel = taskList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        
        // Prevent deleting manager-assigned tasks
        if ("MANAGER".equals(sel.getAssignmentType())) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Manager-assigned tasks cannot be deleted. You can only mark them as complete.");
            a.showAndWait();
            return;
        }
        
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Delete Task");
        a.setHeaderText("Delete task");
        a.setContentText("Are you sure you want to delete this task?");
        a.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            try {
                taskDao.delete(sel.getId());
                tasks.remove(sel);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onRefresh() { loadTasks(); }

    @FXML
    private void onLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Logout");
        confirmation.setHeaderText("Logout?");
        confirmation.setContentText("Are you sure you want to logout?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Go back to login screen
                FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) editButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateEditDeleteButtonStatus() {
        Task selected = taskList.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            return;
        }
        
        // Disable edit/delete for manager-assigned tasks
        boolean isManagerTask = "MANAGER".equals(selected.getAssignmentType());
        editButton.setDisable(isManagerTask);
        deleteButton.setDisable(isManagerTask);
        
        if (isManagerTask) {
            editButton.setStyle("-fx-padding: 8 12; -fx-font-size: 11; -fx-background-color: #9ca3af; -fx-text-fill: white;");
            deleteButton.setStyle("-fx-padding: 8 12; -fx-font-size: 11; -fx-background-color: #9ca3af; -fx-text-fill: white;");
        } else {
            editButton.setStyle("-fx-padding: 8 12; -fx-font-size: 11; -fx-background-color: #3b82f6; -fx-text-fill: white;");
            deleteButton.setStyle("-fx-padding: 8 12; -fx-font-size: 11; -fx-background-color: #ef4444; -fx-text-fill: white;");
        }
    }

    private void openEditor(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("/fxml/add_edit.fxml"));
            Parent root = loader.load();
            AddEditController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            ctrl.setTask(task);

            Stage s = new Stage();
            s.initModality(Modality.APPLICATION_MODAL);
            s.setTitle(task == null ? "Add Task" : "Edit Task");
            s.setScene(new Scene(root));
            s.showAndWait();

            if (ctrl.isSaved()) loadTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTaskCheckboxToggle(Task task) {
        try {
            boolean isCurrentlyMarked = task.isMarkedForCompletion();
            
            // Find the assignment for this task and employee
            com.doable.dao.AssignmentDao assignmentDao = new com.doable.dao.AssignmentDao();
            com.doable.model.Assignment assignment = assignmentDao.findByTaskAndEmployee(task.getId(), currentUser.getId());
            
            if (assignment == null) {
                System.out.println("DEBUG: No assignment found for task " + task.getId() + " and employee " + currentUser.getId());
                return;
            }
            
            if (!isCurrentlyMarked) {
                // Task is being marked complete - update assignment
                assignment.setMarkedForCompletion(true);
                assignment.setCompletedAt(System.currentTimeMillis());
                System.out.println("DEBUG: Marking assignment " + assignment.getId() + " as complete");
                
                // Log action
                com.doable.dao.ActionLogDao logDao = new com.doable.dao.ActionLogDao();
                logDao.save(new com.doable.model.ActionLog(
                    currentUser.getId(),
                    "COMPLETE_TASK",
                    "Completed task \"" + task.getTitle() + "\" (ID:" + task.getId() + ")"
                ));
            } else {
                // Task is being unmarked - mark assignment as pending again
                assignment.setMarkedForCompletion(false);
                assignment.setCompletedAt(0);
                System.out.println("DEBUG: Marking assignment " + assignment.getId() + " as pending");
                
                // Log action
                com.doable.dao.ActionLogDao logDao = new com.doable.dao.ActionLogDao();
                logDao.save(new com.doable.model.ActionLog(
                    currentUser.getId(),
                    "INCOMPLETE_TASK",
                    "Marked task \"" + task.getTitle() + "\" (ID:" + task.getId() + ") as incomplete"
                ));
            }
            
            // Save the assignment (not the task)
            assignmentDao.save(assignment);
            
            // Refresh the display
            loadTasks();
            loadCompletedManagerTasks();  // Also update completed tasks view
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to update task assignment");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void renewTask(Task task) {
        if (task.getDueDate() == null) return;
        
        String repeatRule = task.getRepeatRule();
        if (repeatRule == null || "NONE".equals(repeatRule)) return;
        
        LocalDateTime currentDueDate = task.getDueDate();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newDueDate = currentDueDate;
        
        if ("DAILY".equals(repeatRule)) {
            // Add 1 day, keep adding until the due date is in the future
            while (newDueDate.isBefore(now) || newDueDate.equals(now)) {
                newDueDate = newDueDate.plusDays(1);
            }
        } else if ("WEEKLY".equals(repeatRule)) {
            // Add 1 week, keep adding until the due date is in the future
            while (newDueDate.isBefore(now) || newDueDate.equals(now)) {
                newDueDate = newDueDate.plusWeeks(1);
            }
        } else if (repeatRule.startsWith("CUSTOM_")) {
            // Handle custom day-based repeat: "CUSTOM_MON,WED,FRI"
            try {
                String[] parts = repeatRule.split("_");
                if (parts.length > 1) {
                    String[] selectedDays = parts[1].split(",");
                    Set<DayOfWeek> customDays = new HashSet<>();
                    
                    for (String day : selectedDays) {
                        switch (day.trim()) {
                            case "MON":
                                customDays.add(DayOfWeek.MONDAY);
                                break;
                            case "TUE":
                                customDays.add(DayOfWeek.TUESDAY);
                                break;
                            case "WED":
                                customDays.add(DayOfWeek.WEDNESDAY);
                                break;
                            case "THU":
                                customDays.add(DayOfWeek.THURSDAY);
                                break;
                            case "FRI":
                                customDays.add(DayOfWeek.FRIDAY);
                                break;
                            case "SAT":
                                customDays.add(DayOfWeek.SATURDAY);
                                break;
                            case "SUN":
                                customDays.add(DayOfWeek.SUNDAY);
                                break;
                        }
                    }
                    
                    // Find next occurrence of one of the selected days
                    newDueDate = newDueDate.plusDays(1);
                    while (!customDays.contains(newDueDate.getDayOfWeek())) {
                        newDueDate = newDueDate.plusDays(1);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing custom repeat rule: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (repeatRule.startsWith("EVERY_")) {
            // Handle formats like "EVERY_2_DAYS", "EVERY_3_WEEKS"
            try {
                String[] parts = repeatRule.split("_");
                if (parts.length >= 3) {
                    int interval = Integer.parseInt(parts[1]);
                    String unit = parts[2];
                    
                    if ("DAYS".equals(unit)) {
                        while (newDueDate.isBefore(now) || newDueDate.equals(now)) {
                            newDueDate = newDueDate.plusDays(interval);
                        }
                    } else if ("WEEKS".equals(unit)) {
                        while (newDueDate.isBefore(now) || newDueDate.equals(now)) {
                            newDueDate = newDueDate.plusWeeks(interval);
                        }
                    } else if ("MONTHS".equals(unit)) {
                        while (newDueDate.isBefore(now) || newDueDate.equals(now)) {
                            newDueDate = newDueDate.plusMonths(interval);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        
        task.setDueDate(newDueDate);
    }

    // periodic check for due tasks and show system tray notification if supported
    private void startReminderChecker() {
        Thread checkerThread = new Thread(() -> {
            while (true) {
                try {
                    List<Task> list = taskDao.findAll();
                    LocalDateTime now = LocalDateTime.now();
                    
                    for (Task t : list) {
                        if (!t.isCompleted() && !t.isMarkedForCompletion() && t.getDueDate() != null) {
                            long diffMs = Duration.between(now, t.getDueDate()).toMillis();
                            long taskId = t.getId();
                            
                            // Auto-complete task if reminder time has passed
                            if (diffMs < 0) {
                                t.setCompleted(true);
                                try {
                                    taskDao.save(t);
                                    System.out.println("Auto-completed task: " + t.getTitle());
                                } catch (SQLException e) {
                                    System.err.println("Error auto-completing task: " + e.getMessage());
                                }
                            }
                            
                            // Notification at EXACT TIME (-2000 to 2000 ms, only once)
                            if (diffMs >= -2000 && diffMs <= 2000 && !notifiedAtExactTimeIds.contains(taskId)) {
                                notifiedAtExactTimeIds.add(taskId);
                                System.out.println("Triggering reminder for: " + t.getTitle());
                                NotificationUtil.displayNotification("Task Reminder", 
                                    "Task '" + t.getTitle() + "' is due now!");
                            }
                            // Clean up old notifications (task is more than 5 minutes past due)
                            else if (diffMs < -300000) {
                                notifiedTaskIds.remove(taskId);
                                notifiedAtExactTimeIds.remove(taskId);
                            }
                        }
                    }
                    
                    Thread.sleep(500); // Check every 500ms
                } catch (SQLException | InterruptedException ex) {
                    System.err.println("Reminder checker error: " + ex.getMessage());
                    try {
                        Thread.sleep(1000); // Wait before retrying
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "ReminderCheckerThread");
        
        checkerThread.setDaemon(true);
        checkerThread.start();
    }

    private void loadCompletedManagerTasks() {
        try {
            if (currentUser == null) {
                allCompletedManagerTasks = List.of();
            } else {
                // Load completed assignments for this employee
                com.doable.dao.AssignmentDao assignmentDao = new com.doable.dao.AssignmentDao();
                List<com.doable.model.Assignment> assignments = assignmentDao.findByEmployeeId(currentUser.getId());
                
                List<Task> completedTasks = new java.util.ArrayList<>();
                for (com.doable.model.Assignment assignment : assignments) {
                    if (assignment.isMarkedForCompletion()) {  // Only completed assignments
                        Task task = taskDao.findById(assignment.getTaskId());
                        if (task != null) {
                            // Set marked_for_completion from assignment
                            task.setMarkedForCompletion(true);
                            completedTasks.add(task);
                        }
                    }
                }
                allCompletedManagerTasks = completedTasks;
                System.out.println("DEBUG: Loaded " + allCompletedManagerTasks.size() + " completed tasks for employee: " + currentUser.getUsername());
                
                // Load categories into filter
                Set<String> categories = new HashSet<>();
                categories.add("All Categories");
                for (Task task : allCompletedManagerTasks) {
                    if (task.getCategoryName() != null && !task.getCategoryName().isEmpty()) {
                        categories.add(task.getCategoryName());
                    }
                }
                completedCategoryFilter.setItems(FXCollections.observableArrayList(categories));
                completedCategoryFilter.setValue("All Categories");
            }
            applyCompletedFilters();
        } catch (SQLException e) {
            statusLabel.setText("Error loading completed tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void applyCompletedFilters() {
        try {
            java.time.LocalDate dateFilter = completedDateFilter.getValue();
            String categoryFilter = completedCategoryFilter.getValue();
            
            List<Task> filtered = allCompletedManagerTasks.stream()
                .filter(t -> {
                    // Apply date filter if selected
                    if (dateFilter != null && t.getDueDate() != null) {
                        java.time.LocalDate taskDate = t.getDueDate().toLocalDate();
                        return taskDate.equals(dateFilter);
                    }
                    return true;
                })
                .filter(t -> {
                    // Apply category filter
                    if (categoryFilter != null && !"All Categories".equals(categoryFilter)) {
                        return categoryFilter.equals(t.getCategoryName());
                    }
                    return true;
                })
                .toList();
            
            completedTasks.setAll(filtered);
        } catch (Exception e) {
            statusLabel.setText("Error filtering completed tasks: " + e.getMessage());
        }
    }
    
    @FXML
    private void onClearCompletedFilters() {
        completedDateFilter.setValue(null);
        completedCategoryFilter.setValue("All Categories");
        loadCompletedManagerTasks();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("HomeController: Current user set to " + user.getUsername());
            loadTasks();
            loadCompletedManagerTasks();
        }
    }
}
