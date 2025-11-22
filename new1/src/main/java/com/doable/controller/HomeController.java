package com.doable.controller;

import com.doable.util.NotificationUtil;
import com.doable.dao.CategoryDao;
import com.doable.model.Category;
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
    @FXML private ChoiceBox<String> filterChoice;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private Label statusLabel;

    private final TaskDao taskDao = new TaskDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private List<Task> allTasks = List.of();
    private final Set<Long> notifiedTaskIds = new HashSet<>(); // Track which tasks have been notified
    private final Set<Long> notifiedAtExactTimeIds = new HashSet<>(); // Track tasks notified at exact due time

    public void initialize() {
        filterChoice.getItems().addAll("All", "Pending", "Completed");
        filterChoice.setValue("All");
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
                    taskLabel.setText(display);

                    // Create HBox with checkbox and task label
                    HBox hbox = new HBox(10);
                    hbox.setStyle("-fx-padding: 5;");
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
        
        loadTasks();
        startReminderChecker();
    }
    
    private void loadCategories() {
        try {
            categoryFilter.getItems().clear();
            // Add "All Categories" option
            Category allCat = new Category(0, "All Categories", "#ffffff");
            categoryFilter.getItems().add(allCat);
            categoryFilter.getItems().addAll(categoryDao.findAll());
            categoryFilter.setValue(allCat);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        try {
            allTasks = taskDao.findAll();
            applyFilters();
        } catch (SQLException e) {
            statusLabel.setText("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void applyFilters() {
        String statusFilter = filterChoice.getValue();
        Category selectedCategory = categoryFilter.getValue();
        
        List<Task> filtered = allTasks.stream()
            .filter(t -> {
                // Apply status filter
                if ("Pending".equals(statusFilter)) {
                    return !t.isCompleted();
                } else if ("Completed".equals(statusFilter)) {
                    return t.isCompleted();
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
                // Sort: incomplete (markedForCompletion=false) first, then completed (markedForCompletion=true)
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

            loadTasks(); // Refresh in case any data was imported
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open settings");
            alert.setContentText(e.getMessage());
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
        openEditor(sel);
    }

    @FXML
    private void onDeleteTask() {
        Task sel = taskList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
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

    private void openEditor(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(HomeController.class.getResource("/fxml/add_edit.fxml"));
            Parent root = loader.load();
            AddEditController ctrl = loader.getController();
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
            // Toggle the marked for completion state
            task.setMarkedForCompletion(!task.isMarkedForCompletion());
            
            if (task.isMarkedForCompletion()) {
                // Task is marked for completion - suppress reminders
                // No changes to due date needed, just update the flag
                taskDao.save(task);
            } else {
                // Task is unmarked - renew it based on repeat rule
                renewTask(task);
                taskDao.save(task);
            }
            
            // Refresh the display
            loadTasks();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to update task");
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
}
