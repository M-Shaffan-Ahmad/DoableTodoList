package com.doable.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import com.doable.model.User;
import com.doable.model.UserRole;
import com.doable.model.Task;
import com.doable.model.Category;
import com.doable.dao.UserDao;
import com.doable.dao.TaskDao;
import com.doable.dao.CategoryDao;
import javafx.stage.FileChooser;

import java.nio.file.Files;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ManagerDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;

    // Create Employee fields
    @FXML private TextField employeeUsername;
    @FXML private PasswordField employeePassword;
    @FXML private TextField employeeEmail;
    @FXML private TextField employeePhone;
    @FXML private TextField employeeJobTitle;
    @FXML private Button createEmployeeButton;
    @FXML private Label employeeErrorLabel;
    @FXML private Label employeeSuccessLabel;

    // Employees Table
    @FXML private TableView<EmployeeRow> employeesTable;

    // Task creation fields
    @FXML private TextField taskTitle;
    @FXML private TextArea taskDescription;
    @FXML private ComboBox<String> taskCategoryCombo;
    @FXML private Button createTaskButton;
    @FXML private Label taskErrorLabel;
    @FXML private Label taskSuccessLabel;

    // Enhanced task creation fields
    @FXML private DatePicker taskDatePicker;
    @FXML private Spinner<Integer> taskHourSpinner;
    @FXML private Spinner<Integer> taskMinuteSpinner;
    @FXML private ComboBox<String> taskAmpmCombo;
    @FXML private ToggleButton taskRepeatDaily;
    @FXML private ToggleButton taskRepeatWeekly;
    @FXML private ToggleButton taskRepeatMonthly;
    @FXML private ToggleButton taskRepeatCustom;
    @FXML private Label taskNoRepeatLabel;
    @FXML private Button taskSetReminderButton;
    @FXML private Label taskReminderLabel;

    // Task assignment fields
    @FXML private TextField assignTaskIdInput;
    @FXML private ComboBox<String> assignTaskCombo;
    @FXML private ComboBox<String> filterTaskCategoryCombo;
    @FXML private RadioButton assignAllRadio;
    @FXML private RadioButton assignSelectedRadio;
    @FXML private ComboBox<String> assignEmployeeCombo;
    @FXML private Button assignTaskButton;
    @FXML private Label assignErrorLabel;
    @FXML private Label assignSuccessLabel;

    // Created tasks table
    @FXML private ComboBox<String> filterCreatedTasksCategoryCombo;
    @FXML private TableView<TaskRow> createdTasksTable;

    // Category fields
    @FXML private TextField categoryName;
    @FXML private Button createCategoryButton;
    @FXML private Label categoryErrorLabel;
    @FXML private Label categorySuccessLabel;

    // Categories table
    @FXML private TableView<CategoryRow> categoriesTable;

    // Progress tracking fields
    @FXML private ComboBox<String> filterEmployeeCombo;
    @FXML private ComboBox<String> filterTaskCombo;
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private Button applyFiltersButton;
    @FXML private Button clearFiltersButton;
    @FXML private TableView<ProgressRow> progressTable;

    private User currentUser;
    private List<Task> allTasks;
    private List<Category> allCategories;
    private Map<String, Long> employeeFilterMap = new HashMap<>();  // Maps display names to employee IDs
    private Map<String, Long> taskFilterMap = new HashMap<>();  // Maps display names to task IDs

    @FXML
    public void initialize() {
        // Setup filter combo box
        filterStatusCombo.setItems(FXCollections.observableArrayList("All", "Completed", "Pending"));
        
        // Setup task creation fields
        setupTaskCreationFields();
        
        // Employee handlers
        createEmployeeButton.setOnAction(event -> handleCreateEmployee());
        logoutButton.setOnAction(event -> handleLogout());
        setupEmployeesTable();
        
        // Add double-click listener to edit employees
        employeesTable.setRowFactory(tv -> {
            TableRow<EmployeeRow> row = new TableRow<EmployeeRow>() {
                @Override
                protected void updateItem(EmployeeRow item, boolean empty) {
                    super.updateItem(item, empty);
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditEmployee(row.getItem());
                }
            });
            return row;
        });

        // Task handlers
        createTaskButton.setOnAction(event -> handleCreateTask());
        assignTaskButton.setOnAction(event -> handleAssignTask());
        setupCreatedTasksTable();
        
        // Setup task ID input listener
        if (assignTaskIdInput != null) {
            assignTaskIdInput.setOnKeyReleased(event -> {
                String input = assignTaskIdInput.getText().trim();
                if (!input.isEmpty()) {
                    try {
                        long taskId = Long.parseLong(input);
                        // Find and select task with this ID
                        for (int i = 0; i < allTasks.size(); i++) {
                            if (allTasks.get(i).getId() == taskId) {
                                assignTaskCombo.setValue("[ID:" + taskId + "] " + allTasks.get(i).getTitle());
                                break;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Invalid ID, ignore
                    }
                }
            });
        }

        // Category handlers
        createCategoryButton.setOnAction(event -> handleCreateCategory());
        setupCategoriesTable();

        // Progress tracking handlers
        applyFiltersButton.setOnAction(event -> loadTaskProgress());
        clearFiltersButton.setOnAction(event -> clearFilters());
        setupProgressTable();

        // Setup context menus for tables
        setupEmployeesTable();
        
        // Radio button toggle for assignment
        ToggleGroup assignGroup = new ToggleGroup();
        assignAllRadio.setToggleGroup(assignGroup);
        assignSelectedRadio.setToggleGroup(assignGroup);
        assignAllRadio.setSelected(true);
        assignEmployeeCombo.setDisable(true);
        
        assignAllRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            assignEmployeeCombo.setDisable(newVal);
        });
        assignSelectedRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            assignEmployeeCombo.setDisable(!newVal);
        });
    }

    private void setupEmployeesTable() {
        // Clear any existing columns from FXML
        employeesTable.getColumns().clear();
        
        TableColumn<EmployeeRow, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().id));
        idCol.setPrefWidth(50);

        TableColumn<EmployeeRow, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().username));
        usernameCol.setPrefWidth(120);

        TableColumn<EmployeeRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().email));
        emailCol.setPrefWidth(150);

        TableColumn<EmployeeRow, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().phone));
        phoneCol.setPrefWidth(120);

        TableColumn<EmployeeRow, String> jobTitleCol = new TableColumn<>("Job Title");
        jobTitleCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().jobTitle));
        jobTitleCol.setPrefWidth(150);

        TableColumn<EmployeeRow, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().created));
        createdCol.setPrefWidth(150);

        employeesTable.getColumns().addAll(idCol, usernameCol, emailCol, phoneCol, jobTitleCol, createdCol);
        
        // Add context menu for edit/delete
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        
        editItem.setOnAction(event -> {
            EmployeeRow selected = employeesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEditEmployee(selected);
            }
        });
        
        deleteItem.setOnAction(event -> {
            EmployeeRow selected = employeesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDeleteEmployee(selected);
            }
        });
        
        contextMenu.getItems().addAll(editItem, deleteItem);
        employeesTable.setContextMenu(contextMenu);
    }

    private void loadEmployees() {
        ObservableList<EmployeeRow> data = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());

        for (User employee : UserDao.getEmployeesByManager(currentUser.getId())) {
            String created = Instant.ofEpochMilli(employee.getCreatedAt())
                    .atZone(ZoneId.systemDefault())
                    .format(formatter);
            data.add(new EmployeeRow(
                    employee.getId(),
                    employee.getUsername(),
                    employee.getEmail(),
                    employee.getPhoneNumber(),
                    employee.getJobTitle() != null ? employee.getJobTitle() : "N/A",
                    created
            ));
        }
        employeesTable.setItems(data);
    }

    private void handleCreateEmployee() {
        String username = employeeUsername.getText().trim();
        String password = employeePassword.getText();
        String email = employeeEmail.getText().trim();
        String phone = employeePhone.getText().trim();
        String jobTitle = employeeJobTitle.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty() || jobTitle.isEmpty()) {
            employeeErrorLabel.setText("All fields are required");
            employeeSuccessLabel.setText("");
            return;
        }

        if (email.length() < 5 || !email.contains("@")) {
            employeeErrorLabel.setText("Invalid email format");
            employeeSuccessLabel.setText("");
            return;
        }

        try {
            User employee = new User(username, password, email, phone, UserRole.EMPLOYEE);
            employee.setJobTitle(jobTitle);
            employee.setCreatedBy(currentUser.getId());
            employee.setCreatedAt(System.currentTimeMillis());

            UserDao.createUser(employee);

            // Clear fields
            employeeUsername.clear();
            employeePassword.clear();
            employeeEmail.clear();
            employeePhone.clear();
            employeeJobTitle.clear();

            employeeErrorLabel.setText("");
            employeeSuccessLabel.setText("Employee created successfully!");

            // Reload table
            loadEmployees();
        } catch (IllegalArgumentException e) {
            employeeErrorLabel.setText(e.getMessage());
            employeeSuccessLabel.setText("");
        } catch (Exception e) {
            employeeErrorLabel.setText("Error: " + e.getMessage());
            employeeSuccessLabel.setText("");
        }
    }

    private void handleLogout() {
        try {
            LoginController.setCurrentUser(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Doable - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onGenerateReport() {
        try {
            // Create report window
            Stage reportStage = new Stage();
            reportStage.setTitle("Task Report - " + currentUser.getUsername());
            reportStage.setWidth(1000);
            reportStage.setHeight(700);

            // Create main layout
            VBox mainLayout = new VBox(10);
            mainLayout.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

            // Title
            Label titleLabel = new Label("Task Report for Manager: " + currentUser.getUsername());
            titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

            // Get statistics
            com.doable.dao.ReportDao reportDao = new com.doable.dao.ReportDao();
            Map<String, Object> stats = reportDao.getTaskStatistics(currentUser.getId());
            Map<String, Integer> categoryBreakdown = reportDao.getCategoryBreakdown(currentUser.getId());

            // Statistics section
            VBox statsBox = createStatisticsBox(stats);

            // Category breakdown
            VBox categoryBox = createCategoryBreakdownBox(categoryBreakdown);

            // Task details table
            VBox tableBox = createTaskDetailsBox(reportDao.getTaskReportsByManager(currentUser.getId()));

            // Button panel
            HBox buttonPanel = new HBox(10);
            buttonPanel.setStyle("-fx-padding: 10; -fx-alignment: CENTER_RIGHT;");

            Button exportCSVButton = new Button("Export to CSV");
            exportCSVButton.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #10b981; -fx-text-fill: white;");
            exportCSVButton.setOnAction(e -> {
                try {
                    exportReportToCSV(reportDao.getTaskReportsByManager(currentUser.getId()));
                } catch (Exception ex) {
                    showAlert("Error", "Failed to export: " + ex.getMessage());
                }
            });

            Button exportPDFButton = new Button("Export to PDF");
            exportPDFButton.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #f59e0b; -fx-text-fill: white;");
            exportPDFButton.setOnAction(e -> {
                try {
                    exportReportToPDF(reportDao.getTaskReportsByManager(currentUser.getId()), stats, categoryBreakdown);
                } catch (Exception ex) {
                    showAlert("Error", "Failed to export PDF: " + ex.getMessage());
                }
            });

            Button printButton = new Button("Print Report");
            printButton.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #3b82f6; -fx-text-fill: white;");
            printButton.setOnAction(e -> printReport(reportStage));

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #6b7280; -fx-text-fill: white;");
            closeButton.setOnAction(e -> reportStage.close());

            buttonPanel.getChildren().addAll(exportCSVButton, exportPDFButton, printButton, closeButton);

            // Scroll pane for content
            ScrollPane scrollPane = new ScrollPane();
            VBox contentBox = new VBox(15);
            contentBox.setStyle("-fx-padding: 10;");
            contentBox.getChildren().addAll(statsBox, categoryBox, tableBox);
            scrollPane.setContent(contentBox);
            scrollPane.setFitToWidth(true);

            mainLayout.getChildren().addAll(titleLabel, scrollPane, buttonPanel);

            Scene scene = new Scene(mainLayout);
            reportStage.setScene(scene);
            reportStage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createStatisticsBox(Map<String, Object> stats) {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-background-color: white; -fx-padding: 15;");

        Label titleLabel = new Label("Summary Statistics");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        HBox statsRow = new HBox(20);
        statsRow.setStyle("-fx-alignment: CENTER_LEFT;");

        int totalTasks = (int) stats.getOrDefault("totalTasks", 0);
        int totalAssignments = (int) stats.getOrDefault("totalAssignments", 0);
        int completed = (int) stats.getOrDefault("completed", 0);
        int pending = (int) stats.getOrDefault("pending", 0);

        double completionRate = totalAssignments > 0 ? (completed * 100.0 / totalAssignments) : 0;

        Label stat1 = new Label("Total Tasks Created: " + totalTasks);
        Label stat2 = new Label("Total Assignments: " + totalAssignments);
        Label stat3 = new Label("Completed: " + completed);
        Label stat4 = new Label("Pending: " + pending);
        Label stat5 = new Label(String.format("Completion Rate: %.1f%%", completionRate));

        stat1.setStyle("-fx-font-size: 12; -fx-text-fill: #374151;");
        stat2.setStyle("-fx-font-size: 12; -fx-text-fill: #374151;");
        stat3.setStyle("-fx-font-size: 12; -fx-text-fill: #10b981; -fx-font-weight: bold;");
        stat4.setStyle("-fx-font-size: 12; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
        stat5.setStyle("-fx-font-size: 12; -fx-text-fill: #3b82f6; -fx-font-weight: bold;");

        statsRow.getChildren().addAll(stat1, stat2, stat3, stat4, stat5);
        box.getChildren().addAll(titleLabel, statsRow);

        return box;
    }

    private VBox createCategoryBreakdownBox(Map<String, Integer> breakdown) {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-background-color: white; -fx-padding: 15;");

        Label titleLabel = new Label("Tasks by Category");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        HBox categoryRow = new HBox(20);
        categoryRow.setStyle("-fx-alignment: CENTER_LEFT;");

        if (breakdown.isEmpty()) {
            Label emptyLabel = new Label("No tasks assigned to categories");
            emptyLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #6b7280;");
            categoryRow.getChildren().add(emptyLabel);
        } else {
            breakdown.forEach((category, count) -> {
                Label categoryLabel = new Label(category + ": " + count);
                categoryLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #374151;");
                categoryRow.getChildren().add(categoryLabel);
            });
        }

        box.getChildren().addAll(titleLabel, categoryRow);
        return box;
    }

    private VBox createTaskDetailsBox(List<com.doable.model.TaskReport> reports) {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-background-color: white; -fx-padding: 15;");

        Label titleLabel = new Label("Task Details");
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Create table
        TableView<com.doable.model.TaskReport> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<com.doable.model.TaskReport, String> titleCol = new TableColumn<>("Task");
        titleCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getTaskTitle()));
        titleCol.setPrefWidth(150);

        TableColumn<com.doable.model.TaskReport, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getCategory()));
        categoryCol.setPrefWidth(100);

        TableColumn<com.doable.model.TaskReport, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        TableColumn<com.doable.model.TaskReport, String> completionCol = new TableColumn<>("Completion");
        completionCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getCompletionPercentageFormatted()));
        completionCol.setPrefWidth(100);

        TableColumn<com.doable.model.TaskReport, String> assignmentsCol = new TableColumn<>("Assignments");
        assignmentsCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
            p.getValue().getCompletedAssignments() + "/" + p.getValue().getTotalAssignments()));
        assignmentsCol.setPrefWidth(100);

        TableColumn<com.doable.model.TaskReport, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getFormattedCreatedDate()));
        createdCol.setPrefWidth(140);

        table.getColumns().addAll(titleCol, categoryCol, statusCol, completionCol, assignmentsCol, createdCol);
        table.setItems(javafx.collections.FXCollections.observableArrayList(reports));

        box.getChildren().addAll(titleLabel, table);
        return box;
    }

    private void exportReportToCSV(List<com.doable.model.TaskReport> reports) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Report as CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
            fileChooser.setInitialFileName("task_report_" + System.currentTimeMillis() + ".csv");

            File file = fileChooser.showSaveDialog(new Stage());
            if (file == null) return;

            StringBuilder csv = new StringBuilder();
            csv.append("Task ID,Title,Category,Status,Completion %,Completed/Total Assignments,Created Date,Due Date\n");

            for (com.doable.model.TaskReport report : reports) {
                csv.append(report.getTaskId()).append(",");
                csv.append("\"").append(escapeCSV(report.getTaskTitle())).append("\",");
                csv.append("\"").append(escapeCSV(report.getCategory())).append("\",");
                csv.append(report.getStatus()).append(",");
                csv.append(String.format("%.1f", report.getCompletionPercentage())).append(",");
                csv.append(report.getCompletedAssignments()).append("/").append(report.getTotalAssignments()).append(",");
                csv.append(report.getFormattedCreatedDate()).append(",");
                csv.append(report.getFormattedDueDate()).append("\n");
            }

            Files.writeString(file.toPath(), csv.toString());
            showAlert("Success", "Report exported to " + file.getName());
        } catch (Exception e) {
            showAlert("Error", "Failed to export report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportReportToPDF(List<com.doable.model.TaskReport> reports, Map<String, Object> stats, Map<String, Integer> breakdown) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Report as PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
            fileChooser.setInitialFileName("task_report_" + System.currentTimeMillis() + ".pdf");

            File file = fileChooser.showSaveDialog(new Stage());
            if (file == null) return;

            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(file);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
            document.setMargins(20, 20, 20, 20);

            // Title
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph("Task Report");
            title.setFontSize(24).setBold().setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(title);

            // Manager and date info
            com.itextpdf.layout.element.Paragraph info = new com.itextpdf.layout.element.Paragraph(
                "Manager: " + currentUser.getUsername() + "\nGenerated: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())
            );
            info.setFontSize(11).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER).setMarginBottom(15);
            document.add(info);

            // Summary Statistics Section
            com.itextpdf.layout.element.Paragraph statsTitle = new com.itextpdf.layout.element.Paragraph("Summary Statistics");
            statsTitle.setFontSize(14).setBold().setMarginTop(10).setMarginBottom(10);
            document.add(statsTitle);

            int totalTasks = (int) stats.getOrDefault("totalTasks", 0);
            int totalAssignments = (int) stats.getOrDefault("totalAssignments", 0);
            int completed = (int) stats.getOrDefault("completed", 0);
            int pending = (int) stats.getOrDefault("pending", 0);
            double completionRate = totalAssignments > 0 ? (completed * 100.0 / totalAssignments) : 0;

            com.itextpdf.layout.element.Table statsTable = new com.itextpdf.layout.element.Table(5);
            statsTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Total Tasks")).setBold());
            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Total Assignments")).setBold());
            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Completed")).setBold());
            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Pending")).setBold());
            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Completion Rate")).setBold());

            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(totalTasks))));
            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(totalAssignments))));
            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(completed))));
            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(pending))));
            statsTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.format("%.1f%%", completionRate))));

            document.add(statsTable);
            document.add(new com.itextpdf.layout.element.Paragraph("").setMarginBottom(10));

            // Category Breakdown Section
            if (!breakdown.isEmpty()) {
                com.itextpdf.layout.element.Paragraph categoryTitle = new com.itextpdf.layout.element.Paragraph("Tasks by Category");
                categoryTitle.setFontSize(14).setBold().setMarginTop(10).setMarginBottom(10);
                document.add(categoryTitle);

                com.itextpdf.layout.element.Table categoryTable = new com.itextpdf.layout.element.Table(2);
                categoryTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
                categoryTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Category")).setBold());
                categoryTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Count")).setBold());

                breakdown.forEach((category, count) -> {
                    categoryTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(category)));
                    categoryTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(count))));
                });

                document.add(categoryTable);
                document.add(new com.itextpdf.layout.element.Paragraph("").setMarginBottom(10));
            }

            // Task Details Section
            com.itextpdf.layout.element.Paragraph taskTitle = new com.itextpdf.layout.element.Paragraph("Task Details");
            taskTitle.setFontSize(14).setBold().setMarginTop(10).setMarginBottom(10);
            document.add(taskTitle);

            com.itextpdf.layout.element.Table taskTable = new com.itextpdf.layout.element.Table(7);
            taskTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Task")).setBold());
            taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Category")).setBold());
            taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Status")).setBold());
            taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Completion %")).setBold());
            taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Assignments")).setBold());
            taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Created")).setBold());
            taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Due")).setBold());

            for (com.doable.model.TaskReport report : reports) {
                taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getTaskTitle())));
                taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getCategory())));
                taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getStatus())));
                taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getCompletionPercentageFormatted())));
                taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(
                    report.getCompletedAssignments() + "/" + report.getTotalAssignments())));
                taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getFormattedCreatedDate())));
                taskTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getFormattedDueDate())));
            }

            document.add(taskTable);

            document.close();
            showAlert("Success", "Report exported to " + file.getName());
        } catch (Exception e) {
            showAlert("Error", "Failed to export PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printReport(Stage stage) {
        showAlert("Info", "Print functionality will be available soon");
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + " (" + user.getDepartment() + ")");
            loadEmployees();
            loadTaskCategories();
            loadCreatedCategories();
            loadCategoriesForFilter();
            loadCreatedTasks();
            loadTasksForAssignment();
            loadProgressFilters();
            loadTaskProgress();
        }
    }

    private void loadTaskCategories() {
        try {
            CategoryDao categoryDao = new CategoryDao();
            List<Category> categories = categoryDao.findAll();
            
            ObservableList<String> categoryNames = FXCollections.observableArrayList();
            for (Category cat : categories) {
                categoryNames.add(cat.getName());
            }
            taskCategoryCombo.setItems(categoryNames);
        } catch (Exception e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void handleEditEmployee(EmployeeRow employeeRow) {
        // Create dialog for editing employee
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Employee");
        dialog.setHeaderText("Edit Employee Information");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField(employeeRow.username);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Leave empty to keep current password");
        TextField emailField = new TextField(employeeRow.email);
        TextField phoneField = new TextField(employeeRow.phone);
        TextField jobTitleField = new TextField(employeeRow.jobTitle);

        usernameField.setPrefWidth(300);
        passwordField.setPrefWidth(300);
        emailField.setPrefWidth(300);
        phoneField.setPrefWidth(300);
        jobTitleField.setPrefWidth(300);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Job Title:"), 0, 4);
        grid.add(jobTitleField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            String newUsername = usernameField.getText().trim();
            String newPassword = passwordField.getText();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();
            String newJobTitle = jobTitleField.getText().trim();

            // Validation
            if (newUsername.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() || newJobTitle.isEmpty()) {
                showAlert("Validation Error", "All fields except password are required");
                return;
            }

            if (newEmail.length() < 5 || !newEmail.contains("@")) {
                showAlert("Validation Error", "Invalid email format");
                return;
            }

            try {
                // Get the full user object and update it
                User employee = UserDao.findById(employeeRow.id);
                if (employee != null) {
                    employee.setUsername(newUsername);
                    if (!newPassword.isEmpty()) {
                        employee.setPassword(newPassword);
                    }
                    employee.setEmail(newEmail);
                    employee.setPhoneNumber(newPhone);
                    employee.setJobTitle(newJobTitle);
                    UserDao.updateUser(employee);
                    loadEmployees();
                    showAlert("Success", "Employee updated successfully!");
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to update employee: " + e.getMessage());
            }
        }
    }

    private void handleDeleteEmployee(EmployeeRow employeeRow) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Employee");
        confirmDialog.setContentText("Are you sure you want to delete this employee? This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UserDao.deleteUser(employeeRow.id);
                loadEmployees();
                showAlert("Success", "Employee deleted successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to delete employee: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Task and Category Management for Manager
    public void createTask(String title, String description, long employeeId) {
        try {
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setUserId(employeeId);
            task.setCreatedBy(currentUser.getId());
            task.setCompleted(false);
            task.setRepeatRule("NONE");
            
            TaskDao taskDao = new TaskDao();
            taskDao.save(task);
            showAlert("Success", "Task created for employee successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to create task: " + e.getMessage());
        }
    }

    public void createCategory(String name, String color) {
        try {
            Category category = new Category(name);
            category.setCreatedBy(currentUser.getId());
            
            CategoryDao categoryDao = new CategoryDao();
            categoryDao.save(category);
            showAlert("Success", "Category created successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to create category: " + e.getMessage());
        }
    }

    // ============ TASK MANAGEMENT METHODS ============

    private void setupTaskCreationFields() {
        // Setup time spinners
        SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 12);
        taskHourSpinner.setValueFactory(hourFactory);
        
        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        minuteFactory.setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return String.format("%02d", object);
            }
            @Override
            public Integer fromString(String string) {
                return Integer.parseInt(string);
            }
        });
        taskMinuteSpinner.setValueFactory(minuteFactory);
        
        // Setup AM/PM combo
        taskAmpmCombo.setItems(FXCollections.observableArrayList("AM", "PM"));
        taskAmpmCombo.setValue("AM");
        
        // Setup repeat toggle group
        ToggleGroup repeatGroup = new ToggleGroup();
        taskRepeatDaily.setToggleGroup(repeatGroup);
        taskRepeatWeekly.setToggleGroup(repeatGroup);
        taskRepeatMonthly.setToggleGroup(repeatGroup);
        taskRepeatCustom.setToggleGroup(repeatGroup);
        
        // Handle repeat button selection
        repeatGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == taskRepeatCustom) {
                showCustomRepeatDaysDialog();
            } else if (newVal != null) {
                taskNoRepeatLabel.setText("");
            } else {
                taskNoRepeatLabel.setText("No repeat");
            }
        });
        
        // Set reminder button handler
        taskSetReminderButton.setOnAction(event -> handleSetTaskReminder());
    }
    
    private String selectedCustomRepeatDays = "";
    
    private void showCustomRepeatDaysDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Days for Custom Repeat");
        dialog.setHeaderText("Select which days to repeat");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] dayAbbrev = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        CheckBox[] dayCheckBoxes = new CheckBox[7];
        
        for (int i = 0; i < 7; i++) {
            dayCheckBoxes[i] = new CheckBox(days[i]);
            content.getChildren().add(dayCheckBoxes[i]);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < 7; i++) {
                    if (dayCheckBoxes[i].isSelected()) {
                        if (result.length() > 0) result.append(",");
                        result.append(dayAbbrev[i]);
                    }
                }
                return result.toString();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(days_selected -> {
            selectedCustomRepeatDays = days_selected;
            taskNoRepeatLabel.setText("Custom: " + days_selected);
        });
    }
    
    private void showCustomRepeatDaysDialogForEdit(Label label, String[] customRepeatDays) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Custom Repeat Days");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] dayAbbrev = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        CheckBox[] dayCheckBoxes = new CheckBox[7];
        
        // Pre-select days from current custom repeat rule
        if (!customRepeatDays[0].isEmpty()) {
            String[] selectedDays = customRepeatDays[0].split(",");
            for (String day : selectedDays) {
                for (int i = 0; i < dayAbbrev.length; i++) {
                    if (dayAbbrev[i].equals(day.trim())) {
                        dayCheckBoxes[i] = new CheckBox(days[i]);
                        dayCheckBoxes[i].setSelected(true);
                        break;
                    }
                }
            }
        }
        
        for (int i = 0; i < 7; i++) {
            if (dayCheckBoxes[i] == null) {
                dayCheckBoxes[i] = new CheckBox(days[i]);
            }
            content.getChildren().add(dayCheckBoxes[i]);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < 7; i++) {
                    if (dayCheckBoxes[i].isSelected()) {
                        if (result.length() > 0) result.append(",");
                        result.append(dayAbbrev[i]);
                    }
                }
                return result.toString();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(days_selected -> {
            customRepeatDays[0] = days_selected;
            label.setText("Custom: " + days_selected);
        });
    }
    
    private void handleSetTaskReminder() {
        // TODO: Implement reminder dialog for tasks
        taskReminderLabel.setText("Reminder set for task");
    }

    private void handleCreateTask() {
        String title = taskTitle.getText().trim();
        String description = taskDescription.getText().trim();
        String categoryStr = taskCategoryCombo.getValue();

        if (title.isEmpty()) {
            taskErrorLabel.setText("Task title is required");
            taskSuccessLabel.setText("");
            return;
        }

        try {
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setCreatedBy(currentUser.getId());
            task.setCompleted(false);

            // Set due date and time
            if (taskDatePicker.getValue() != null) {
                int hour = taskHourSpinner.getValue();
                int minute = taskMinuteSpinner.getValue();
                String ampm = taskAmpmCombo.getValue();
                
                // Convert to 24-hour format
                if ("PM".equals(ampm) && hour != 12) {
                    hour += 12;
                } else if ("AM".equals(ampm) && hour == 12) {
                    hour = 0;
                }
                
                java.time.LocalDateTime dateTime = java.time.LocalDateTime.of(
                    taskDatePicker.getValue(),
                    java.time.LocalTime.of(hour, minute)
                );
                task.setDueDate(dateTime);
            }

            // Set repeat rule
            ToggleButton selectedRepeat = (ToggleButton) taskRepeatDaily.getToggleGroup().getSelectedToggle();
            if (selectedRepeat == taskRepeatDaily) {
                task.setRepeatRule("DAILY");
            } else if (selectedRepeat == taskRepeatWeekly) {
                task.setRepeatRule("WEEKLY");
            } else if (selectedRepeat == taskRepeatMonthly) {
                task.setRepeatRule("MONTHLY");
            } else if (selectedRepeat == taskRepeatCustom) {
                if (!selectedCustomRepeatDays.isEmpty()) {
                    task.setRepeatRule("CUSTOM:" + selectedCustomRepeatDays);
                } else {
                    task.setRepeatRule("CUSTOM");
                }
            } else {
                task.setRepeatRule("NONE");
            }

            if (categoryStr != null && !categoryStr.isEmpty()) {
                try {
                    CategoryDao catDao = new CategoryDao();
                    List<Category> categories = catDao.findAll();
                    for (Category c : categories) {
                        if (c.getName().equals(categoryStr)) {
                            task.setCategoryId(c.getId());
                            break;
                        }
                    }
                } catch (Exception e) {}
            }

            TaskDao taskDao = new TaskDao();
            taskDao.save(task);
            
            // Log action
            com.doable.dao.ActionLogDao logDao = new com.doable.dao.ActionLogDao();
            logDao.save(new com.doable.model.ActionLog(
                currentUser.getId(),
                "CREATE_TASK",
                "Created task: " + task.getTitle() + " (ID:" + task.getId() + ")"
            ));

            // Clear fields
            taskTitle.clear();
            taskDescription.clear();
            taskCategoryCombo.setValue(null);
            taskDatePicker.setValue(null);
            taskHourSpinner.getValueFactory().setValue(12);
            taskMinuteSpinner.getValueFactory().setValue(0);
            taskAmpmCombo.setValue("AM");
            taskRepeatDaily.getToggleGroup().selectToggle(null);
            taskReminderLabel.setText("No reminder set");

            taskErrorLabel.setText("");
            taskSuccessLabel.setText("Task created successfully! Now assign it to employees.");
            
            loadCreatedTasks();
            loadTasksForAssignment();
        } catch (Exception e) {
            taskErrorLabel.setText("Error: " + e.getMessage());
            taskSuccessLabel.setText("");
        }
    }

    private void loadTasksForAssignment() {
        try {
            if (currentUser == null) return;
            
            TaskDao taskDao = new TaskDao();
            allTasks = taskDao.findByCreatedBy(currentUser.getId());
            
            ObservableList<String> taskNames = FXCollections.observableArrayList();
            for (Task task : allTasks) {
                // Only show tasks with valid title (filter deleted tasks)
                if (task.getTitle() != null && !task.getTitle().isEmpty()) {
                    // Display task with ID for better identification
                    taskNames.add("[ID:" + task.getId() + "] " + task.getTitle());
                }
            }
            assignTaskCombo.setItems(taskNames);
            
            // Also load employees for the assignment section
            loadEmployeesForAssignment();
            
            // Load categories for filtering tasks
            loadCategoriesForTaskFilter();
        } catch (Exception e) {
            showAlert("Error", "Failed to load tasks: " + e.getMessage());
        }
    }
    
    private void loadCategoriesForTaskFilter() {
        try {
            CategoryDao categoryDao = new CategoryDao();
            List<Category> categories = categoryDao.findAll();
            
            ObservableList<String> categoryNames = FXCollections.observableArrayList();
            categoryNames.add("All Categories");
            for (Category cat : categories) {
                categoryNames.add(cat.getName());
            }
            filterTaskCategoryCombo.setItems(categoryNames);
            filterTaskCategoryCombo.setValue("All Categories");
            
            // Add listener to filter tasks when category changes
            filterTaskCategoryCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                filterTasksByCategory();
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
        }
    }
    
    private void filterTasksByCategory() {
        try {
            String selectedCategory = filterTaskCategoryCombo.getValue();
            if (selectedCategory == null || "All Categories".equals(selectedCategory)) {
                // Show all tasks
                loadTasksForAssignment();
                return;
            }
            
            // Filter tasks by category
            CategoryDao categoryDao = new CategoryDao();
            Category cat = null;
            for (Category c : categoryDao.findAll()) {
                if (c.getName().equals(selectedCategory)) {
                    cat = c;
                    break;
                }
            }
            
            if (cat == null) return;
            
            ObservableList<String> taskNames = FXCollections.observableArrayList();
            for (Task task : allTasks) {
                if (task.getTitle() != null && !task.getTitle().isEmpty() && task.getCategoryId() == cat.getId()) {
                    // Display task with ID for better identification
                    taskNames.add("[ID:" + task.getId() + "] " + task.getTitle());
                }
            }
            assignTaskCombo.setItems(taskNames);
        } catch (Exception e) {
            showAlert("Error", "Failed to filter tasks: " + e.getMessage());
        }
    }

    private void loadEmployeesForAssignment() {
        try {
            if (currentUser == null) return;
            
            UserDao userDao = new UserDao();
            List<User> employees = userDao.getEmployeesByManager(currentUser.getId());
            System.out.println("DEBUG loadEmployeesForAssignment: Manager ID=" + currentUser.getId() + ", has " + employees.size() + " employees");
            for (User emp : employees) {
                System.out.println("  - Employee: " + emp.getUsername() + " (ID=" + emp.getId() + ")");
            }
            
            ObservableList<String> employeeNames = FXCollections.observableArrayList();
            for (User emp : employees) {
                employeeNames.add(emp.getUsername());
            }
            assignEmployeeCombo.setItems(employeeNames);
        } catch (Exception e) {
            showAlert("Error", "Failed to load employees: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAssignTask() {
        String selectedTaskDisplay = assignTaskCombo.getValue();
        if (selectedTaskDisplay == null || selectedTaskDisplay.isEmpty()) {
            assignErrorLabel.setText("Please select a task");
            assignSuccessLabel.setText("");
            return;
        }

        try {
            // Extract task ID from display string "[ID:123] Task Title"
            long taskId = -1;
            if (selectedTaskDisplay.startsWith("[ID:")) {
                int endIdx = selectedTaskDisplay.indexOf("]");
                String idStr = selectedTaskDisplay.substring(4, endIdx);
                taskId = Long.parseLong(idStr);
            }
            
            Task taskToAssign = null;
            for (Task t : allTasks) {
                if (t.getId() == taskId) {
                    taskToAssign = t;
                    break;
                }
            }

            if (taskToAssign == null) {
                assignErrorLabel.setText("Task not found");
                assignSuccessLabel.setText("");
                return;
            }

            if (assignAllRadio.isSelected()) {
                // Assign to all employees - create assignment entries for each
                UserDao userDao = new UserDao();
                List<User> employees = userDao.getEmployeesByManager(currentUser.getId());
                System.out.println("DEBUG handleAssignTask: Manager has " + employees.size() + " employees");
                
                int count = 0;
                com.doable.dao.AssignmentDao assignmentDao = new com.doable.dao.AssignmentDao();
                for (User employee : employees) {
                    try {
                        // Check if assignment already exists
                        com.doable.model.Assignment existingAssignment = assignmentDao.findByTaskAndEmployee(taskToAssign.getId(), employee.getId());
                        if (existingAssignment != null) {
                            System.out.println("DEBUG: Assignment already exists for employee " + employee.getUsername() + ", skipping");
                            continue; // Skip if already assigned
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG: Error checking for existing assignment: " + e.getMessage());
                    }
                    
                    // Create an assignment entry instead of duplicating the task
                    com.doable.model.Assignment assignment = new com.doable.model.Assignment(
                        taskToAssign.getId(),
                        employee.getId(),
                        currentUser.getId()
                    );
                    try {
                        assignmentDao.save(assignment);
                        System.out.println("DEBUG: Creating assignment for employee " + employee.getUsername() + " (ID=" + employee.getId() + ")");
                        
                        // Log action
                        com.doable.dao.ActionLogDao logDao = new com.doable.dao.ActionLogDao();
                        logDao.save(new com.doable.model.ActionLog(
                            currentUser.getId(),
                            "ASSIGN_TASK",
                            "Assigned task \"" + taskToAssign.getTitle() + "\" (ID:" + taskToAssign.getId() + ") to " + employee.getUsername()
                        ));
                        count++;
                    } catch (Exception ex) {
                        System.out.println("DEBUG: Error creating assignment for " + employee.getUsername() + ": " + ex.getMessage());
                    }
                }
                
                assignErrorLabel.setText("");
                assignSuccessLabel.setText("Task assigned to " + count + " employees!");
            } else {
                // Assign to selected employee
                String selectedEmployee = assignEmployeeCombo.getValue();
                if (selectedEmployee == null || selectedEmployee.isEmpty()) {
                    assignErrorLabel.setText("Please select an employee");
                    assignSuccessLabel.setText("");
                    return;
                }

                UserDao userDao = new UserDao();
                User employee = userDao.findByUsername(selectedEmployee);
                System.out.println("DEBUG: Selected employee " + selectedEmployee + ", found employee: " + (employee != null ? employee.getId() : "NULL"));
                
                if (employee == null) {
                    assignErrorLabel.setText("Employee not found");
                    assignSuccessLabel.setText("");
                    return;
                }

                com.doable.dao.AssignmentDao assignmentDao = new com.doable.dao.AssignmentDao();
                
                try {
                    // Check if assignment already exists
                    com.doable.model.Assignment existingAssignment = assignmentDao.findByTaskAndEmployee(taskToAssign.getId(), employee.getId());
                    if (existingAssignment != null) {
                        assignErrorLabel.setText("This task is already assigned to " + selectedEmployee);
                        assignSuccessLabel.setText("");
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: Error checking for existing assignment: " + e.getMessage());
                }

                // Create an assignment entry instead of duplicating the task
                com.doable.model.Assignment assignment = new com.doable.model.Assignment(
                    taskToAssign.getId(),
                    employee.getId(),
                    currentUser.getId()
                );
                try {
                    assignmentDao.save(assignment);
                    System.out.println("DEBUG: Created assignment for task ID=" + taskToAssign.getId() + ", employee=" + employee.getUsername());
                    
                    // Log action
                    com.doable.dao.ActionLogDao logDao = new com.doable.dao.ActionLogDao();
                    logDao.save(new com.doable.model.ActionLog(
                        currentUser.getId(),
                        "ASSIGN_TASK",
                        "Assigned task \"" + taskToAssign.getTitle() + "\" (ID:" + taskToAssign.getId() + ") to " + selectedEmployee
                    ));
                    
                    assignErrorLabel.setText("");
                    assignSuccessLabel.setText("Task assigned to " + selectedEmployee + "!");
                } catch (Exception ex) {
                    assignErrorLabel.setText("Error assigning task: " + ex.getMessage());
                    assignSuccessLabel.setText("");
                }
            }

            assignTaskCombo.setValue(null);
            assignEmployeeCombo.setValue(null);
            loadTaskProgress();
        } catch (Exception e) {
            assignErrorLabel.setText("Error: " + e.getMessage());
            assignSuccessLabel.setText("");
        }
    }

    public void handleUnassignTask(long taskId, long employeeId, long assignmentId) {
        try {
            // Delete the assignment
            com.doable.dao.AssignmentDao assignmentDao = new com.doable.dao.AssignmentDao();
            assignmentDao.delete(assignmentId);
            
            // Get task and employee details for logging
            TaskDao taskDao = new TaskDao();
            Task task = taskDao.findById(taskId);
            UserDao userDao = new UserDao();
            User employee = userDao.findById(employeeId);
            
            // Log action
            com.doable.dao.ActionLogDao logDao = new com.doable.dao.ActionLogDao();
            logDao.save(new com.doable.model.ActionLog(
                currentUser.getId(),
                "UNASSIGN_TASK",
                "Unassigned task \"" + (task != null ? task.getTitle() : "Unknown") + "\" (ID:" + taskId + ") from " + (employee != null ? employee.getUsername() : "Unknown Employee")
            ));
            
            System.out.println("DEBUG: Unassigned task ID=" + taskId + " from employee ID=" + employeeId);
            
            // Refresh the progress table
            loadTaskProgress();
            
            showAlert("Success", "Task unassigned successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to unassign task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupCreatedTasksTable() {
        createdTasksTable.getColumns().clear();
        
        TableColumn<TaskRow, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().id));
        idCol.setPrefWidth(40);

        TableColumn<TaskRow, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().title));
        titleCol.setPrefWidth(150);

        TableColumn<TaskRow, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().description));
        descCol.setPrefWidth(180);

        TableColumn<TaskRow, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().category));
        catCol.setPrefWidth(100);

        TableColumn<TaskRow, String> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().dueDate));
        dueCol.setPrefWidth(120);

        TableColumn<TaskRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().status));
        statusCol.setPrefWidth(80);

        TableColumn<TaskRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<TaskRow, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setPrefWidth(60);
                editBtn.setStyle("-fx-font-size: 10; -fx-padding: 5;");
                deleteBtn.setPrefWidth(60);
                deleteBtn.setStyle("-fx-font-size: 10; -fx-padding: 5; -fx-background-color: #ef4444; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    TaskRow row = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> handleEditTask(row));
                    deleteBtn.setOnAction(e -> handleDeleteTask(row));
                    HBox hbox = new HBox(5, editBtn, deleteBtn);
                    setGraphic(hbox);
                }
            }
        });

        createdTasksTable.getColumns().addAll(idCol, titleCol, descCol, catCol, dueCol, statusCol, actionsCol);
    }

    private void loadCreatedTasks() {
        try {
            if (currentUser == null) {
                System.out.println("DEBUG: currentUser is null in loadCreatedTasks");
                return;
            }
            
            System.out.println("DEBUG: Loading created tasks for user: " + currentUser.getId() + " (" + currentUser.getUsername() + ")");
            
            TaskDao taskDao = new TaskDao();
            List<Task> tasks = taskDao.findAll();
            
            System.out.println("DEBUG: Total tasks in database: " + tasks.size());
            
            // Store all created tasks for filtering
            List<Task> createdTasks = new ArrayList<>();
            Set<String> categoryNames = new HashSet<>();
            categoryNames.add("All Categories");
            
            for (Task task : tasks) {
                if (task.getCreatedBy() == currentUser.getId()) {
                    createdTasks.add(task);
                    if (task.getCategoryId() > 0) {
                        try {
                            CategoryDao catDao = new CategoryDao();
                            Category cat = catDao.findById(task.getCategoryId());
                            if (cat != null) {
                                categoryNames.add(cat.getName());
                            }
                        } catch (Exception e) {}
                    }
                }
            }
            
            // Load categories into filter dropdown
            if (filterCreatedTasksCategoryCombo != null) {
                filterCreatedTasksCategoryCombo.setItems(FXCollections.observableArrayList(categoryNames));
                filterCreatedTasksCategoryCombo.setValue("All Categories");
                filterCreatedTasksCategoryCombo.getSelectionModel().selectedItemProperty().removeListener((obs, old, newVal) -> filterCreatedTasksByCategory());
                filterCreatedTasksCategoryCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> filterCreatedTasksByCategory());
            }
            
            // Display all created tasks
            displayCreatedTasks(createdTasks);
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in loadCreatedTasks: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void filterCreatedTasksByCategory() {
        try {
            if (currentUser == null) return;
            
            String selectedCategory = filterCreatedTasksCategoryCombo.getValue();
            
            TaskDao taskDao = new TaskDao();
            List<Task> allCreatedTasks = new ArrayList<>();
            
            for (Task task : taskDao.findAll()) {
                if (task.getCreatedBy() == currentUser.getId()) {
                    allCreatedTasks.add(task);
                }
            }
            
            // Filter by category if not "All Categories"
            if (selectedCategory != null && !"All Categories".equals(selectedCategory)) {
                List<Task> filtered = new ArrayList<>();
                for (Task task : allCreatedTasks) {
                    if (task.getCategoryId() > 0) {
                        try {
                            CategoryDao catDao = new CategoryDao();
                            Category cat = catDao.findById(task.getCategoryId());
                            if (cat != null && cat.getName().equals(selectedCategory)) {
                                filtered.add(task);
                            }
                        } catch (Exception e) {}
                    }
                }
                displayCreatedTasks(filtered);
            } else {
                displayCreatedTasks(allCreatedTasks);
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in filterCreatedTasksByCategory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void displayCreatedTasks(List<Task> tasks) {
        ObservableList<TaskRow> rows = FXCollections.observableArrayList();
        for (Task task : tasks) {
            String category = "No Category";
            if (task.getCategoryId() > 0) {
                try {
                    CategoryDao catDao = new CategoryDao();
                    Category cat = catDao.findById(task.getCategoryId());
                    if (cat != null) {
                        category = cat.getName();
                    }
                } catch (Exception e) {}
            }
            
            String dueDate = task.getDueDate() != null ? task.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Not set";
            String status = task.isCompleted() ? "Completed" : "Pending";
            
            System.out.println("DEBUG: Adding task to table: " + task.getTitle());
            rows.add(new TaskRow(task.getId(), task.getTitle(), task.getDescription(), category, dueDate, status, ""));
        }
        System.out.println("DEBUG: Total rows to display: " + rows.size());
        createdTasksTable.setItems(rows);
    }

    private void handleEditTask(TaskRow row) {
        try {
            // Fetch the full task object from database
            TaskDao taskDao = new TaskDao();
            Task task = null;
            for (Task t : taskDao.findAll()) {
                if (t.getId() == row.id) {
                    task = t;
                    break;
                }
            }
            
            if (task == null) {
                showAlert("Error", "Could not find task to edit");
                return;
            }
            
            // Create edit dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Task");
            dialog.setHeaderText("Edit Task: " + task.getTitle());
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            
            // Title field
            TextField editTitle = new TextField(task.getTitle());
            grid.add(new Label("Title:"), 0, 0);
            grid.add(editTitle, 1, 0);
            
            // Description field
            TextArea editDescription = new TextArea(task.getDescription() != null ? task.getDescription() : "");
            editDescription.setWrapText(true);
            editDescription.setPrefHeight(80);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(editDescription, 1, 1);
            
            // Category field
            ComboBox<String> editCategoryCombo = new ComboBox<>();
            try {
                CategoryDao catDao = new CategoryDao();
                List<Category> categories = catDao.findAll();
                ObservableList<String> categoryNames = FXCollections.observableArrayList();
                categoryNames.add("No Category");
                for (Category c : categories) {
                    categoryNames.add(c.getName());
                }
                editCategoryCombo.setItems(categoryNames);
                
                if (task.getCategoryId() > 0) {
                    Category cat = catDao.findById(task.getCategoryId());
                    if (cat != null) {
                        editCategoryCombo.setValue(cat.getName());
                    }
                } else {
                    editCategoryCombo.setValue("No Category");
                }
            } catch (Exception e) {
                editCategoryCombo.setValue("No Category");
            }
            grid.add(new Label("Category:"), 0, 2);
            grid.add(editCategoryCombo, 1, 2);
            
            // Date picker
            DatePicker editDatePicker = new DatePicker();
            if (task.getDueDate() != null) {
                editDatePicker.setValue(task.getDueDate().toLocalDate());
            }
            grid.add(new Label("Due Date:"), 0, 3);
            grid.add(editDatePicker, 1, 3);
            
            // Time spinners
            Spinner<Integer> editHourSpinner = new Spinner<>(1, 12, 12);
            Spinner<Integer> editMinuteSpinner = new Spinner<>(0, 59, 0);
            ComboBox<String> editAmpmCombo = new ComboBox<>();
            editAmpmCombo.setItems(FXCollections.observableArrayList("AM", "PM"));
            editAmpmCombo.setValue("AM");
            
            if (task.getDueDate() != null) {
                int hour = task.getDueDate().getHour();
                int minute = task.getDueDate().getMinute();
                
                String ampm = "AM";
                if (hour >= 12) {
                    ampm = "PM";
                    if (hour > 12) hour -= 12;
                } else if (hour == 0) {
                    hour = 12;
                }
                
                editHourSpinner.getValueFactory().setValue(hour);
                editMinuteSpinner.getValueFactory().setValue(minute);
                editAmpmCombo.setValue(ampm);
            }
            
            HBox timeBox = new HBox(5);
            timeBox.getChildren().addAll(editHourSpinner, new Label(":"), editMinuteSpinner, editAmpmCombo);
            grid.add(new Label("Time:"), 0, 4);
            grid.add(timeBox, 1, 4);
            
            // Repeat options
            ToggleGroup editRepeatGroup = new ToggleGroup();
            ToggleButton editRepeatDaily = new ToggleButton("Daily");
            ToggleButton editRepeatWeekly = new ToggleButton("Weekly");
            ToggleButton editRepeatMonthly = new ToggleButton("Monthly");
            ToggleButton editRepeatCustom = new ToggleButton("Custom");
            
            editRepeatDaily.setToggleGroup(editRepeatGroup);
            editRepeatWeekly.setToggleGroup(editRepeatGroup);
            editRepeatMonthly.setToggleGroup(editRepeatGroup);
            editRepeatCustom.setToggleGroup(editRepeatGroup);
            
            // Set selected repeat based on current task
            String repeatRule = task.getRepeatRule() != null ? task.getRepeatRule() : "NONE";
            if (repeatRule.equals("DAILY")) {
                editRepeatDaily.setSelected(true);
            } else if (repeatRule.equals("WEEKLY")) {
                editRepeatWeekly.setSelected(true);
            } else if (repeatRule.equals("MONTHLY")) {
                editRepeatMonthly.setSelected(true);
            } else if (repeatRule.startsWith("CUSTOM")) {
                editRepeatCustom.setSelected(true);
            }
            
            HBox repeatBox = new HBox(5);
            repeatBox.getChildren().addAll(editRepeatDaily, editRepeatWeekly, editRepeatMonthly, editRepeatCustom);
            
            // Label for custom repeat days
            Label editCustomDaysLabel = new Label("No custom days selected");
            String[] customRepeatDays = new String[1];
            customRepeatDays[0] = "";
            
            // Extract custom days from current repeat rule if present
            if (repeatRule.startsWith("CUSTOM:")) {
                customRepeatDays[0] = repeatRule.substring(7);
                editCustomDaysLabel.setText("Custom: " + customRepeatDays[0]);
            }
            
            // Add listener for custom button to show dialog
            editRepeatCustom.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    showCustomRepeatDaysDialogForEdit(editCustomDaysLabel, customRepeatDays);
                }
            });
            
            grid.add(new Label("Repeat:"), 0, 5);
            grid.add(repeatBox, 1, 5);
            grid.add(new Label("Custom Days:"), 0, 6);
            grid.add(editCustomDaysLabel, 1, 6);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Update task properties
                task.setTitle(editTitle.getText().trim());
                task.setDescription(editDescription.getText().trim());
                
                // Update category
                String selectedCategory = editCategoryCombo.getValue();
                if (selectedCategory != null && !selectedCategory.equals("No Category")) {
                    try {
                        CategoryDao catDao = new CategoryDao();
                        List<Category> categories = catDao.findAll();
                        for (Category c : categories) {
                            if (c.getName().equals(selectedCategory)) {
                                task.setCategoryId(c.getId());
                                break;
                            }
                        }
                    } catch (Exception e) {}
                } else {
                    task.setCategoryId(0);
                }
                
                // Update date and time
                if (editDatePicker.getValue() != null) {
                    int hour = editHourSpinner.getValue();
                    int minute = editMinuteSpinner.getValue();
                    String ampm = editAmpmCombo.getValue();
                    
                    if ("PM".equals(ampm) && hour != 12) {
                        hour += 12;
                    } else if ("AM".equals(ampm) && hour == 12) {
                        hour = 0;
                    }
                    
                    java.time.LocalDateTime dateTime = java.time.LocalDateTime.of(
                        editDatePicker.getValue(),
                        java.time.LocalTime.of(hour, minute)
                    );
                    task.setDueDate(dateTime);
                }
                
                // Update repeat rule
                ToggleButton selectedRepeat = (ToggleButton) editRepeatGroup.getSelectedToggle();
                if (selectedRepeat == editRepeatDaily) {
                    task.setRepeatRule("DAILY");
                } else if (selectedRepeat == editRepeatWeekly) {
                    task.setRepeatRule("WEEKLY");
                } else if (selectedRepeat == editRepeatMonthly) {
                    task.setRepeatRule("MONTHLY");
                } else if (selectedRepeat == editRepeatCustom) {
                    if (!customRepeatDays[0].isEmpty()) {
                        task.setRepeatRule("CUSTOM:" + customRepeatDays[0]);
                    } else {
                        task.setRepeatRule("CUSTOM");
                    }
                } else {
                    task.setRepeatRule("NONE");
                }
                
                // Save changes to database
                taskDao.save(task);
                
                showAlert("Success", "Task updated successfully!");
                loadCreatedTasks();
                loadTasksForAssignment();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to edit task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteTask(TaskRow row) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Task");
        confirmation.setHeaderText("Delete Task?");
        confirmation.setContentText("Are you sure you want to delete task '" + row.title + "'?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                TaskDao taskDao = new TaskDao();
                taskDao.delete(row.id);
                showAlert("Success", "Task deleted successfully!");
                loadCreatedTasks();
            } catch (Exception e) {
                showAlert("Error", "Failed to delete task: " + e.getMessage());
            }
        }
    }

    // ============ CATEGORY MANAGEMENT METHODS ============

    private void handleCreateCategory() {
        String name = categoryName.getText().trim();

        if (name.isEmpty()) {
            categoryErrorLabel.setText("Category name is required");
            categorySuccessLabel.setText("");
            return;
        }

        try {
            Category category = new Category(name);
            category.setCreatedBy(currentUser.getId());
            
            CategoryDao categoryDao = new CategoryDao();
            categoryDao.save(category);

            categoryName.clear();

            categoryErrorLabel.setText("");
            categorySuccessLabel.setText("Category created successfully!");
            
            loadCreatedCategories();
            loadCategoriesForFilter();
        } catch (Exception e) {
            categoryErrorLabel.setText("Error: " + e.getMessage());
            categorySuccessLabel.setText("");
        }
    }

    private void loadCreatedCategories() {
        try {
            CategoryDao categoryDao = new CategoryDao();
            allCategories = categoryDao.findAll();
            
            ObservableList<CategoryRow> data = FXCollections.observableArrayList();
            for (Category cat : allCategories) {
                if (cat.getCreatedBy() == currentUser.getId()) {
                    String created = String.format("%tc", System.currentTimeMillis());
                    data.add(new CategoryRow(cat.getId(), cat.getName(), created));
                }
            }
            categoriesTable.setItems(data);
        } catch (Exception e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void handleEditCategory(CategoryRow row) {
        try {
            // Find the full category object
            CategoryDao categoryDao = new CategoryDao();
            Category category = categoryDao.findById(row.id);
            
            if (category == null) {
                showAlert("Error", "Could not find category to edit");
                return;
            }
            
            // Create edit dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Category");
            dialog.setHeaderText("Edit Category: " + category.getName());
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            
            TextField editNameField = new TextField(category.getName());
            grid.add(new Label("Category Name:"), 0, 0);
            grid.add(editNameField, 1, 0);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String newName = editNameField.getText().trim();
                
                if (newName.isEmpty()) {
                    showAlert("Error", "Category name cannot be empty");
                    return;
                }
                
                // Check if name already exists (excluding current category)
                if (categoryDao.isCategoryNameExists(newName, row.id)) {
                    showAlert("Error", "Category name already exists");
                    return;
                }
                
                category.setName(newName);
                categoryDao.save(category);
                
                showAlert("Success", "Category updated successfully!");
                loadCreatedCategories();
                loadCategoriesForFilter();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to edit category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteCategory(CategoryRow row) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Category");
        confirmation.setHeaderText("Delete Category?");
        confirmation.setContentText("Are you sure you want to delete category '" + row.name + "'?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CategoryDao categoryDao = new CategoryDao();
                categoryDao.delete(row.id);
                
                showAlert("Success", "Category deleted successfully!");
                loadCreatedCategories();
                loadCategoriesForFilter();
            } catch (Exception e) {
                showAlert("Error", "Failed to delete category: " + e.getMessage());
            }
        }
    }

    private void setupCategoriesTable() {
        categoriesTable.getColumns().clear();
        
        TableColumn<CategoryRow, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().id));
        idCol.setPrefWidth(50);

        TableColumn<CategoryRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().name));
        nameCol.setPrefWidth(150);

        TableColumn<CategoryRow, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().created));
        createdCol.setPrefWidth(150);

        // Add action buttons column
        TableColumn<CategoryRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<CategoryRow, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setPrefWidth(60);
                editBtn.setStyle("-fx-font-size: 10; -fx-padding: 5;");
                deleteBtn.setPrefWidth(60);
                deleteBtn.setStyle("-fx-font-size: 10; -fx-padding: 5; -fx-text-fill: white; -fx-background-color: #ff4444;");
                
                editBtn.setOnAction(e -> {
                    CategoryRow row = getTableView().getItems().get(getIndex());
                    handleEditCategory(row);
                });
                
                deleteBtn.setOnAction(e -> {
                    CategoryRow row = getTableView().getItems().get(getIndex());
                    handleDeleteCategory(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actionBox = new HBox(5);
                    actionBox.getChildren().addAll(editBtn, deleteBtn);
                    setGraphic(actionBox);
                }
            }
        });

        categoriesTable.getColumns().addAll(idCol, nameCol, createdCol, actionsCol);
    }

    private void loadCategoriesForFilter() {
        try {
            CategoryDao categoryDao = new CategoryDao();
            List<Category> categories = categoryDao.findAll();
            
            ObservableList<String> categoryNames = FXCollections.observableArrayList();
            categoryNames.add("All categories");
            for (Category cat : categories) {
                categoryNames.add(cat.getName());
            }
            filterCategoryCombo.setItems(categoryNames);
            filterCategoryCombo.setValue("All categories");
        } catch (Exception e) {
            showAlert("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    // ============ PROGRESS TRACKING METHODS ============

    private void setupProgressTable() {
        progressTable.getColumns().clear();
        
        TableColumn<ProgressRow, String> empCol = new TableColumn<>("Employee");
        empCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().employeeName));
        empCol.setPrefWidth(150);

        TableColumn<ProgressRow, String> taskCol = new TableColumn<>("Task Title");
        taskCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().taskTitle));
        taskCol.setPrefWidth(200);

        TableColumn<ProgressRow, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().category));
        catCol.setPrefWidth(100);

        TableColumn<ProgressRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().status));
        statusCol.setPrefWidth(100);

        TableColumn<ProgressRow, String> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().progress));
        progressCol.setPrefWidth(80);

        TableColumn<ProgressRow, String> dateCol = new TableColumn<>("Assigned Date");
        dateCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().assignedDate));
        dateCol.setPrefWidth(150);

        // Add Unassign button column
        TableColumn<ProgressRow, Void> unassignCol = new TableColumn<>("Action");
        unassignCol.setPrefWidth(80);
        unassignCol.setCellFactory(param -> new javafx.scene.control.TableCell<ProgressRow, Void>() {
            private final Button unassignButton = new Button("Unassign");
            
            {
                unassignButton.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");
                unassignButton.setOnAction(event -> {
                    ProgressRow row = getTableView().getItems().get(getIndex());
                    handleUnassignTask(row.taskId, row.employeeId, row.assignmentId);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(unassignButton);
                }
            }
        });

        progressTable.getColumns().addAll(empCol, taskCol, catCol, statusCol, progressCol, dateCol, unassignCol);
    }

    private void loadTaskProgress() {
        try {
            String empFilter = filterEmployeeCombo.getValue();
            String taskFilter = filterTaskCombo.getValue();
            String catFilter = filterCategoryCombo.getValue();
            String statusFilter = filterStatusCombo.getValue();
            
            System.out.println("DEBUG loadTaskProgress - Filters: emp=" + empFilter + ", task=" + taskFilter + ", cat=" + catFilter + ", status=" + statusFilter);

            // Get all assignments made by this manager
            com.doable.dao.AssignmentDao assignmentDao = new com.doable.dao.AssignmentDao();
            List<com.doable.model.Assignment> assignments = assignmentDao.findByManagerId(currentUser.getId());
            System.out.println("DEBUG: Manager has " + assignments.size() + " assignments");
            
            ObservableList<ProgressRow> data = FXCollections.observableArrayList();
            TaskDao taskDao = new TaskDao();

            for (com.doable.model.Assignment assignment : assignments) {
                // Get task details
                Task task = taskDao.findById(assignment.getTaskId());
                if (task == null) {
                    System.out.println("DEBUG: Task not found for assignment: taskId=" + assignment.getTaskId());
                    continue;
                }

                // Get employee details
                User employee = UserDao.findById(assignment.getEmployeeId());
                if (employee == null) {
                    System.out.println("DEBUG: Employee not found for assignment: employeeId=" + assignment.getEmployeeId());
                    continue;
                }

                // Apply employee filter
                if (empFilter != null && !empFilter.isEmpty() && !empFilter.equals("All employees")) {
                    Long selectedEmployeeId = employeeFilterMap.get(empFilter);
                    if (selectedEmployeeId == null || employee.getId() != selectedEmployeeId) continue;
                }

                // Apply task filter
                if (taskFilter != null && !taskFilter.isEmpty() && !taskFilter.equals("All tasks")) {
                    Long selectedTaskId = taskFilterMap.get(taskFilter);
                    if (selectedTaskId == null || task.getId() != selectedTaskId) continue;
                }

                // Get status from assignment, not task
                boolean isCompleted = assignment.isMarkedForCompletion();
                
                // Apply status filter
                if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("All")) {
                    if (statusFilter.equals("Completed") && !isCompleted) continue;
                    if (statusFilter.equals("Pending") && isCompleted) continue;
                }

                String category = "None";
                if (task.getCategoryId() > 0) {
                    CategoryDao catDao = new CategoryDao();
                    Category cat = catDao.findById(task.getCategoryId());
                    if (cat != null) category = cat.getName();
                }

                // Apply category filter
                if (catFilter != null && !catFilter.isEmpty() && !catFilter.equals("All categories")) {
                    if (!category.equals(catFilter)) continue;
                }

                String status = isCompleted ? "Completed" : "Pending";
                String progress = isCompleted ? "100%" : "0%";
                String assignedDate = task.getDueDate() != null ? 
                    task.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "Not set";

                System.out.println("DEBUG: Adding to progress table - " + employee.getUsername() + " - " + task.getTitle() + " - " + status);
                data.add(new ProgressRow(task.getId(), employee.getId(), assignment.getId(), employee.getUsername(), task.getTitle(), category, status, progress, assignedDate));
            }

            System.out.println("DEBUG: Final progress table rows: " + data.size());
            progressTable.setItems(data);
        } catch (Exception e) {
            showAlert("Error", "Failed to load progress: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadProgressFilters() {
        try {
            // Clear maps
            employeeFilterMap.clear();
            taskFilterMap.clear();
            
            // Load employees
            ObservableList<String> employees = FXCollections.observableArrayList();
            employees.add("All employees");
            UserDao userDao = new UserDao();
            List<User> empList = userDao.getEmployeesByManager(currentUser.getId());
            System.out.println("DEBUG loadProgressFilters: Manager ID=" + currentUser.getId() + ", has " + empList.size() + " employees");
            for (User emp : empList) {
                System.out.println("  - Employee: " + emp.getUsername() + " (ID=" + emp.getId() + ")");
                String displayName = emp.getUsername() + " (ID:" + emp.getId() + ")";
                employees.add(displayName);
                employeeFilterMap.put(displayName, emp.getId());
            }
            filterEmployeeCombo.setItems(employees);
            filterEmployeeCombo.setValue("All employees");
            
            // Load tasks
            ObservableList<String> tasks = FXCollections.observableArrayList();
            tasks.add("All tasks");
            TaskDao taskDao = new TaskDao();
            List<Task> taskList = taskDao.findByCreatedBy(currentUser.getId());
            for (Task task : taskList) {
                if (task.getTitle() != null && !task.getTitle().isEmpty()) {
                    String displayName = task.getTitle() + " (ID:" + task.getId() + ")";
                    if (!tasks.contains(displayName)) {
                        tasks.add(displayName);
                        taskFilterMap.put(displayName, task.getId());
                    }
                }
            }
            filterTaskCombo.setItems(tasks);
            filterTaskCombo.setValue("All tasks");
            
            // Load categories
            ObservableList<String> categories = FXCollections.observableArrayList();
            categories.add("All categories");
            CategoryDao catDao = new CategoryDao();
            List<Category> catList = catDao.findAll();
            for (Category cat : catList) {
                categories.add(cat.getName());
            }
            filterCategoryCombo.setItems(categories);
            filterCategoryCombo.setValue("All categories");
            
            // Ensure status filter has default value
            filterStatusCombo.setValue("All");
        } catch (Exception e) {
            showAlert("Error", "Failed to load progress filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFilters() {
        filterEmployeeCombo.setValue("All employees");
        filterTaskCombo.setValue("All tasks");
        filterCategoryCombo.setValue("All categories");
        filterStatusCombo.setValue("All");
        loadTaskProgress();
    }

    // Inner class for table row
    public static class EmployeeRow {
        public long id;
        public String username;
        public String email;
        public String phone;
        public String jobTitle;
        public String created;

        public EmployeeRow(long id, String username, String email, String phone, String jobTitle, String created) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.jobTitle = jobTitle;
            this.created = created;
        }
    }

    public static class TaskRow {
        public long id;
        public String title;
        public String description;
        public String category;
        public String dueDate;
        public String status;
        public String created;

        public TaskRow(long id, String title, String description, String category, String dueDate, String status, String created) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.category = category;
            this.dueDate = dueDate;
            this.status = status;
            this.created = created;
        }
    }

    public static class CategoryRow {
        public long id;
        public String name;
        public String created;

        public CategoryRow(long id, String name, String created) {
            this.id = id;
            this.name = name;
            this.created = created;
        }
    }

    public static class ProgressRow {
        public String employeeName;
        public String taskTitle;
        public String category;
        public String status;
        public String progress;
        public String assignedDate;
        public long taskId;  // Track task for unassign
        public long employeeId;  // Track employee for unassign
        public long assignmentId;  // Track assignment for unassign

        public ProgressRow(long taskId, long employeeId, long assignmentId, String employeeName, String taskTitle, String category, String status, String progress, String assignedDate) {
            this.taskId = taskId;
            this.employeeId = employeeId;
            this.assignmentId = assignmentId;
            this.employeeName = employeeName;
            this.taskTitle = taskTitle;
            this.category = category;
            this.status = status;
            this.progress = progress;
            this.assignedDate = assignedDate;
        }
    }
}
