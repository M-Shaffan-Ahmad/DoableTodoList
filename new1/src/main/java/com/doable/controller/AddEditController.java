package com.doable.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import com.doable.dao.TaskDao;
import com.doable.dao.CategoryDao;
import com.doable.model.Task;
import com.doable.model.Category;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddEditController {
    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private ComboBox<String> ampmCombo;
    @FXML private ToggleButton repeatDaily;
    @FXML private ToggleButton repeatWeekly;
    @FXML private ToggleButton repeatMonthly;
    @FXML private ToggleButton repeatCustom;
    @FXML private Label noRepeatLabel;
    @FXML private VBox daysSection;
    @FXML private ToggleButton dayMonday;
    @FXML private ToggleButton dayTuesday;
    @FXML private ToggleButton dayWednesday;
    @FXML private ToggleButton dayThursday;
    @FXML private ToggleButton dayFriday;
    @FXML private ToggleButton daySaturday;
    @FXML private ToggleButton daySunday;
    @FXML private ComboBox<Category> categoryCombo;

    private Task task;
    private boolean saved = false;
    private final TaskDao dao = new TaskDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ToggleGroup repeatGroup = new ToggleGroup();

    public void initialize() {
        // Setup hour and minute spinners with proper formatting
        SpinnerValueFactory.IntegerSpinnerValueFactory hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 12);
        hourFactory.setWrapAround(true);
        hourSpinner.setValueFactory(hourFactory);
        hourSpinner.setEditable(true);
        
        SpinnerValueFactory.IntegerSpinnerValueFactory minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        minuteFactory.setWrapAround(true);
        minuteSpinner.setValueFactory(minuteFactory);
        minuteSpinner.setEditable(true);
        
        // Add text formatter to ensure proper input handling for spinners
        hourSpinner.getEditor().setTextFormatter(new javafx.scene.control.TextFormatter<>(new javafx.util.converter.IntegerStringConverter(), 12, change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }
            try {
                int val = Integer.parseInt(change.getControlNewText());
                if (val >= 1 && val <= 12) {
                    return change;
                }
            } catch (NumberFormatException e) {
                // ignore
            }
            return null;
        }));
        
        minuteSpinner.getEditor().setTextFormatter(new javafx.scene.control.TextFormatter<>(new javafx.util.converter.IntegerStringConverter(), 0, change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }
            try {
                int val = Integer.parseInt(change.getControlNewText());
                if (val >= 0 && val <= 59) {
                    return change;
                }
            } catch (NumberFormatException e) {
                // ignore
            }
            return null;
        }));
        
        // Setup AM/PM combo
        ampmCombo.setItems(FXCollections.observableArrayList("AM", "PM"));
        ampmCombo.setValue("AM");
        
        // Setup repeat toggle group
        repeatDaily.setToggleGroup(repeatGroup);
        repeatWeekly.setToggleGroup(repeatGroup);
        repeatMonthly.setToggleGroup(repeatGroup);
        repeatCustom.setToggleGroup(repeatGroup);

        // Listen to repeat button changes
        repeatDaily.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                noRepeatLabel.setVisible(false);
                daysSection.setVisible(false);
                daysSection.setManaged(false);
            }
        });

        repeatWeekly.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                noRepeatLabel.setVisible(false);
                daysSection.setVisible(true);
                daysSection.setManaged(true);
            }
        });

        repeatMonthly.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                noRepeatLabel.setVisible(false);
                daysSection.setVisible(false);
                daysSection.setManaged(false);
            }
        });

        repeatCustom.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                noRepeatLabel.setVisible(false);
                daysSection.setVisible(true);
                daysSection.setManaged(true);
            }
        });

        
        // Load categories
        loadCategories();
    }
    
    private void loadCategories() {
        try {
            categoryCombo.getItems().clear();
            categoryCombo.getItems().add(new Category(0, "No Category", "#ffffff"));
            categoryCombo.getItems().addAll(categoryDao.findAll());
            categoryCombo.setValue(categoryCombo.getItems().get(0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setTask(Task t) {
        if (t == null) {
            this.task = new Task();
            return;
        }
        this.task = t;
        titleField.setText(t.getTitle());
        descField.setText(t.getDescription());
        if (t.getDueDate() != null) {
            datePicker.setValue(t.getDueDate().toLocalDate());
            java.time.LocalTime lt = t.getDueDate().toLocalTime();
            int hour24 = lt.getHour();
            int minute = lt.getMinute();
            
            // Convert to 12-hour format
            String ampm = hour24 >= 12 ? "PM" : "AM";
            int hour12 = hour24 % 12;
            if (hour12 == 0) hour12 = 12;
            
            // Ensure spinner value factories are initialized before setting values
            if (hourSpinner.getValueFactory() != null) {
                hourSpinner.getValueFactory().setValue(hour12);
            }
            if (minuteSpinner.getValueFactory() != null) {
                minuteSpinner.getValueFactory().setValue(minute);
            }
            // Ensure AM/PM combo has items before setting value
            if (ampmCombo.getItems().isEmpty()) {
                ampmCombo.setItems(FXCollections.observableArrayList("AM", "PM"));
            }
            ampmCombo.setValue(ampm);
        } else {
            // Set default values for new tasks
            if (hourSpinner.getValueFactory() != null) {
                hourSpinner.getValueFactory().setValue(9);
            }
            if (minuteSpinner.getValueFactory() != null) {
                minuteSpinner.getValueFactory().setValue(0);
            }
            // Ensure AM/PM combo has items before setting value
            if (ampmCombo.getItems().isEmpty()) {
                ampmCombo.setItems(FXCollections.observableArrayList("AM", "PM"));
            }
            if (ampmCombo.getValue() == null) {
                ampmCombo.setValue("AM");
            }
        }
        
        String repeatRule = t.getRepeatRule() == null ? "NONE" : t.getRepeatRule();
        
        // Set repeat buttons based on rule
        if (repeatRule.startsWith("CUSTOM_")) {
            repeatCustom.setSelected(true);
            daysSection.setVisible(true);
            daysSection.setManaged(true);
            
            // Parse custom days
            String[] parts = repeatRule.split("_");
            if (parts.length > 1) {
                String[] days = parts[1].split(",");
                for (String day : days) {
                    switch (day.trim()) {
                        case "MON":
                            dayMonday.setSelected(true);
                            break;
                        case "TUE":
                            dayTuesday.setSelected(true);
                            break;
                        case "WED":
                            dayWednesday.setSelected(true);
                            break;
                        case "THU":
                            dayThursday.setSelected(true);
                            break;
                        case "FRI":
                            dayFriday.setSelected(true);
                            break;
                        case "SAT":
                            daySaturday.setSelected(true);
                            break;
                        case "SUN":
                            daySunday.setSelected(true);
                            break;
                    }
                }
            }
        } else {
            // Set repeat buttons based on rule
            switch (repeatRule) {
                case "DAILY":
                    repeatDaily.setSelected(true);
                    break;
                case "WEEKLY":
                    repeatWeekly.setSelected(true);
                    daysSection.setVisible(true);
                    daysSection.setManaged(true);
                    break;
                case "MONTHLY":
                    repeatMonthly.setSelected(true);
                    break;
                default:
                    noRepeatLabel.setVisible(true);
            }
        }
        
        // Set category
        if (t.getCategoryId() > 0) {
            for (Category cat : categoryCombo.getItems()) {
                if (cat.getId() == t.getCategoryId()) {
                    categoryCombo.setValue(cat);
                    break;
                }
            }
        }
    }

    private String buildRepeatRule() {
        // Determine which repeat option is selected
        if (repeatDaily.isSelected()) {
            return "DAILY";
        } else if (repeatWeekly.isSelected()) {
            return "WEEKLY";
        } else if (repeatMonthly.isSelected()) {
            return "MONTHLY";
        } else if (repeatCustom.isSelected()) {
            // Build custom repeat rule from selected days
            StringBuilder days = new StringBuilder();
            if (dayMonday.isSelected()) days.append("MON,");
            if (dayTuesday.isSelected()) days.append("TUE,");
            if (dayWednesday.isSelected()) days.append("WED,");
            if (dayThursday.isSelected()) days.append("THU,");
            if (dayFriday.isSelected()) days.append("FRI,");
            if (daySaturday.isSelected()) days.append("SAT,");
            if (daySunday.isSelected()) days.append("SUN,");
            
            if (days.length() == 0) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Please select at least one day for custom repeat");
                a.showAndWait();
                return "NONE";
            }
            
            // Remove trailing comma
            String daysList = days.substring(0, days.length() - 1);
            System.out.println("Custom repeat rule: CUSTOM_" + daysList);
            return "CUSTOM_" + daysList;
        }
        return "NONE";
    }

    @FXML
    private void onSetReminder() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please select a due date for the reminder");
            a.showAndWait();
            return;
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Reminder set for " + date);
        a.showAndWait();
    }
    
    @FXML
    private void onNewCategory() {
        // Create a dialog to add new category
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Category");
        dialog.setHeaderText("Enter category name:");
        dialog.setContentText("Category name:");
        
        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String categoryName = result.get().trim();
            try {
                // Check if category already exists
                for (Category cat : categoryCombo.getItems()) {
                    if (cat.getName().equalsIgnoreCase(categoryName)) {
                        Alert a = new Alert(Alert.AlertType.WARNING, "Category already exists!");
                        a.showAndWait();
                        return;
                    }
                }
                
                // Create new category with default color
                Category newCat = new Category(categoryName, "#3b82f6");
                categoryDao.save(newCat);
                
                // Add to combo box and select it
                categoryCombo.getItems().add(newCat);
                categoryCombo.setValue(newCat);
                
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Category '" + categoryName + "' created successfully!");
                a.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Error creating category: " + e.getMessage());
                a.showAndWait();
            }
        }
    }

    @FXML
    private void onSave() {
        // Ensure task object exists
        if (task == null) {
            task = new Task();
        }
        
        String title = titleField.getText();
        if (title == null || title.trim().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Title is required");
            a.showAndWait();
            return;
        }
        title = title.trim();
        
        // Check for duplicate title
        try {
            if (dao.isTitleExists(title, task.getId())) {
                Alert a = new Alert(Alert.AlertType.ERROR, "A task with the title '" + title + "' already exists. Please use a different title.");
                a.setTitle("Duplicate Title");
                a.setHeaderText("Cannot save task");
                a.showAndWait();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Error checking for duplicate titles: " + e.getMessage());
            a.showAndWait();
            return;
        }
        
        task.setTitle(title);
        task.setDescription(descField.getText());

        LocalDate date = datePicker.getValue();
        Integer hour = hourSpinner.getValue();
        Integer minute = minuteSpinner.getValue();
        String ampm = ampmCombo.getValue();
        
        // Validate time inputs
        if (hour == null || hour < 1 || hour > 12) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Hour must be between 1 and 12");
            a.showAndWait();
            return;
        }
        if (minute == null || minute < 0 || minute > 59) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Minutes must be between 0 and 59");
            a.showAndWait();
            return;
        }
        if (ampm == null || (!ampm.equals("AM") && !ampm.equals("PM"))) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please select AM or PM");
            a.showAndWait();
            return;
        }
        
        if (date != null) {
            try {
                // Convert 12-hour to 24-hour format
                int hour24 = hour;
                if ("PM".equals(ampm) && hour != 12) {
                    hour24 = hour + 12;
                } else if ("AM".equals(ampm) && hour == 12) {
                    hour24 = 0;
                }
                
                // Validate the resulting time
                if (hour24 < 0 || hour24 > 23) {
                    Alert a = new Alert(Alert.AlertType.WARNING, "Invalid hour value calculated");
                    a.showAndWait();
                    return;
                }
                
                task.setDueDate(LocalDateTime.of(date, LocalTime.of(hour24, minute)));
            } catch (Exception ex) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Invalid time: " + ex.getMessage());
                a.showAndWait();
                return;
            }
        } else {
            // If no date but time is set, use today's date
            try {
                int hour24 = hour;
                if ("PM".equals(ampm) && hour != 12) {
                    hour24 = hour + 12;
                } else if ("AM".equals(ampm) && hour == 12) {
                    hour24 = 0;
                }
                task.setDueDate(LocalDateTime.of(LocalDate.now(), LocalTime.of(hour24, minute)));
            } catch (Exception ex) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Error setting due date: " + ex.getMessage());
                a.showAndWait();
                return;
            }
        }

        task.setRepeatRule(buildRepeatRule());
        
        // Set category
        Category selected = categoryCombo.getValue();
        if (selected != null && selected.getId() > 0) {
            task.setCategoryId(selected.getId());
            task.setCategoryName(selected.getName());
        } else {
            task.setCategoryId(0);
            task.setCategoryName(null);
        }

        try {
            dao.save(task);
            saved = true;
            close();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Error saving task: " + e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage s = (Stage) titleField.getScene().getWindow();
        s.close();
    }

    public boolean isSaved() {
        return saved;
    }
}
