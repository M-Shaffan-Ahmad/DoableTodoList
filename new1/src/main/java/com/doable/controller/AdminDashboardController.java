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
import java.util.Map;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;

    // Create Manager fields
    @FXML private TextField managerUsername;
    @FXML private PasswordField managerPassword;
    @FXML private TextField managerEmail;
    @FXML private TextField managerPhone;
    @FXML private TextField managerDepartment;
    @FXML private Button createManagerButton;
    @FXML private Label managerErrorLabel;
    @FXML private Label managerSuccessLabel;

    // Managers Table
    @FXML private TableView<ManagerRow> managersTable;

    private User currentUser;

    @FXML
    public void initialize() {
        createManagerButton.setOnAction(event -> handleCreateManager());
        logoutButton.setOnAction(event -> handleLogout());
        setupManagersTable();
        
        // Add double-click listener to edit managers
        managersTable.setRowFactory(tv -> {
            TableRow<ManagerRow> row = new TableRow<ManagerRow>() {
                @Override
                protected void updateItem(ManagerRow item, boolean empty) {
                    super.updateItem(item, empty);
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditManager(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupManagersTable() {
        // Clear any existing columns from FXML
        managersTable.getColumns().clear();
        
        TableColumn<ManagerRow, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().id));
        idCol.setPrefWidth(50);

        TableColumn<ManagerRow, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().username));
        usernameCol.setPrefWidth(120);

        TableColumn<ManagerRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().email));
        emailCol.setPrefWidth(150);

        TableColumn<ManagerRow, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().phone));
        phoneCol.setPrefWidth(120);

        TableColumn<ManagerRow, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().department));
        deptCol.setPrefWidth(150);

        TableColumn<ManagerRow, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().created));
        createdCol.setPrefWidth(150);

        managersTable.getColumns().addAll(idCol, usernameCol, emailCol, phoneCol, deptCol, createdCol);
        
        // Add context menu for edit/delete
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        
        editItem.setOnAction(event -> {
            ManagerRow selected = managersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEditManager(selected);
            }
        });
        
        deleteItem.setOnAction(event -> {
            ManagerRow selected = managersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDeleteManager(selected);
            }
        });
        
        contextMenu.getItems().addAll(editItem, deleteItem);
        managersTable.setContextMenu(contextMenu);
    }

    private void loadManagers() {
        ObservableList<ManagerRow> data = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());

        for (User manager : UserDao.getAllManagers()) {
            String created = Instant.ofEpochMilli(manager.getCreatedAt())
                    .atZone(ZoneId.systemDefault())
                    .format(formatter);
            data.add(new ManagerRow(
                    manager.getId(),
                    manager.getUsername(),
                    manager.getEmail(),
                    manager.getPhoneNumber(),
                    manager.getDepartment() != null ? manager.getDepartment() : "N/A",
                    created
            ));
        }
        managersTable.setItems(data);
    }

    private void handleCreateManager() {
        String username = managerUsername.getText().trim();
        String password = managerPassword.getText();
        String email = managerEmail.getText().trim();
        String phone = managerPhone.getText().trim();
        String department = managerDepartment.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty() || department.isEmpty()) {
            managerErrorLabel.setText("All fields are required");
            managerSuccessLabel.setText("");
            return;
        }

        if (email.length() < 5 || !email.contains("@")) {
            managerErrorLabel.setText("Invalid email format");
            managerSuccessLabel.setText("");
            return;
        }

        try {
            User manager = new User(username, password, email, phone, UserRole.MANAGER);
            manager.setDepartment(department);
            manager.setCreatedBy(currentUser.getId());
            manager.setCreatedAt(System.currentTimeMillis());

            UserDao.createUser(manager);

            // Clear fields
            managerUsername.clear();
            managerPassword.clear();
            managerEmail.clear();
            managerPhone.clear();
            managerDepartment.clear();

            managerErrorLabel.setText("");
            managerSuccessLabel.setText("Manager created successfully!");

            // Reload table
            loadManagers();
        } catch (IllegalArgumentException e) {
            managerErrorLabel.setText(e.getMessage());
            managerSuccessLabel.setText("");
        } catch (Exception e) {
            managerErrorLabel.setText("Error: " + e.getMessage());
            managerSuccessLabel.setText("");
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
    private void onGenerateManagersReport() {
        try {
            Stage reportStage = new Stage();
            reportStage.setTitle("Managers Performance Report");
            reportStage.setWidth(1200);
            reportStage.setHeight(800);

            VBox mainLayout = new VBox(10);
            mainLayout.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

            Label titleLabel = new Label("Managers Performance Report");
            titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

            // Get all manager reports
            com.doable.dao.ReportDao reportDao = new com.doable.dao.ReportDao();
            List<com.doable.model.ManagerReport> managerReports = reportDao.getManagerReports();

            // Create table
            TableView<com.doable.model.ManagerReport> table = new TableView<>();
            table.setPrefHeight(500);

            TableColumn<com.doable.model.ManagerReport, String> nameCol = new TableColumn<>("Manager");
            nameCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getManagerName()));
            nameCol.setPrefWidth(120);

            TableColumn<com.doable.model.ManagerReport, Integer> tasksCol = new TableColumn<>("Tasks Created");
            tasksCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().getTotalTasksCreated()));
            tasksCol.setPrefWidth(100);

            TableColumn<com.doable.model.ManagerReport, Integer> assignedCol = new TableColumn<>("Assigned");
            assignedCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().getTotalTasksAssigned()));
            assignedCol.setPrefWidth(100);

            TableColumn<com.doable.model.ManagerReport, Integer> completedCol = new TableColumn<>("Completed");
            completedCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().getTotalAssignmentsCompleted()));
            completedCol.setPrefWidth(100);

            TableColumn<com.doable.model.ManagerReport, Integer> pendingCol = new TableColumn<>("Pending");
            pendingCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().getTotalAssignmentsPending()));
            pendingCol.setPrefWidth(100);

            TableColumn<com.doable.model.ManagerReport, String> completionCol = new TableColumn<>("Completion %");
            completionCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getFormattedCompletionPercentage()));
            completionCol.setPrefWidth(100);

            TableColumn<com.doable.model.ManagerReport, Integer> employeesCol = new TableColumn<>("Employees");
            employeesCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().getTotalEmployeesManaged()));
            employeesCol.setPrefWidth(100);

            TableColumn<com.doable.model.ManagerReport, String> performanceCol = new TableColumn<>("Performance");
            performanceCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getPerformanceStatus()));
            performanceCol.setPrefWidth(120);

            TableColumn<com.doable.model.ManagerReport, Void> detailsCol = new TableColumn<>("Action");
            detailsCol.setPrefWidth(100);
            detailsCol.setCellFactory(param -> new TableCell<com.doable.model.ManagerReport, Void>() {
                private final Button detailsButton = new Button("View Details");

                {
                    detailsButton.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #3b82f6; -fx-text-fill: white;");
                    detailsButton.setOnAction(event -> {
                        com.doable.model.ManagerReport report = getTableView().getItems().get(getIndex());
                        showManagerDetailedReport(report, reportDao);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(detailsButton);
                    }
                }
            });

            table.getColumns().addAll(nameCol, tasksCol, assignedCol, completedCol, pendingCol, completionCol, employeesCol, performanceCol, detailsCol);
            table.setItems(FXCollections.observableArrayList(managerReports));

            // Button panel
            HBox buttonPanel = new HBox(10);
            buttonPanel.setStyle("-fx-padding: 10; -fx-alignment: CENTER_RIGHT;");

            Button exportCSVButton = new Button("Export to CSV");
            exportCSVButton.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #10b981; -fx-text-fill: white;");
            exportCSVButton.setOnAction(e -> exportManagersReportToCSV(managerReports));

            Button exportPDFButton = new Button("Export to PDF");
            exportPDFButton.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #f59e0b; -fx-text-fill: white;");
            exportPDFButton.setOnAction(e -> exportManagersReportToPDF(managerReports));

            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #6b7280; -fx-text-fill: white;");
            closeButton.setOnAction(e -> reportStage.close());

            buttonPanel.getChildren().addAll(exportCSVButton, exportPDFButton, closeButton);

            ScrollPane scrollPane = new ScrollPane(table);
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

    private void showManagerDetailedReport(com.doable.model.ManagerReport managerReport, com.doable.dao.ReportDao reportDao) {
        try {
            Stage detailStage = new Stage();
            detailStage.setTitle("Detailed Report - " + managerReport.getManagerName());
            detailStage.setWidth(1000);
            detailStage.setHeight(700);

            VBox mainLayout = new VBox(10);
            mainLayout.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");

            Label titleLabel = new Label("Detailed Report for Manager: " + managerReport.getManagerName());
            titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

            // Summary box
            VBox summaryBox = new VBox(5);
            summaryBox.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-background-color: white; -fx-padding: 15;");

            Label label1 = new Label("Tasks Created: " + managerReport.getTotalTasksCreated());
            label1.setStyle("-fx-font-size: 12;");
            Label label2 = new Label("Total Assignments: " + managerReport.getTotalTasksAssigned());
            label2.setStyle("-fx-font-size: 12;");
            Label label3 = new Label("Completed: " + managerReport.getTotalAssignmentsCompleted() + " | Pending: " + managerReport.getTotalAssignmentsPending());
            label3.setStyle("-fx-font-size: 12;");
            Label label4 = new Label("Overall Completion: " + managerReport.getFormattedCompletionPercentage());
            label4.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
            Label label5 = new Label("Employees Managed: " + managerReport.getTotalEmployeesManaged());
            label5.setStyle("-fx-font-size: 12;");
            Label label6 = new Label("Performance Status: " + managerReport.getPerformanceStatus());
            label6.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
            Label label7 = new Label("Last Activity: " + managerReport.getFormattedLastActivityDate());
            label7.setStyle("-fx-font-size: 12;");

            summaryBox.getChildren().addAll(label1, label2, label3, label4, label5, label6, label7);

            // Task list for this manager
            List<com.doable.model.TaskReport> taskReports = reportDao.getTaskReportsByManager(managerReport.getManagerId());

            VBox taskBox = new VBox(10);
            taskBox.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-background-color: white; -fx-padding: 15;");

            Label taskTitle = new Label("Tasks Created by " + managerReport.getManagerName());
            taskTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

            TableView<com.doable.model.TaskReport> taskTable = new TableView<>();
            taskTable.setPrefHeight(300);

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

            taskTable.getColumns().addAll(titleCol, categoryCol, statusCol, completionCol, assignmentsCol);
            taskTable.setItems(FXCollections.observableArrayList(taskReports));

            taskBox.getChildren().addAll(taskTitle, taskTable);

            HBox buttonPanel = new HBox(10);
            buttonPanel.setStyle("-fx-alignment: CENTER_RIGHT;");
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-padding: 8 15; -fx-background-color: #6b7280; -fx-text-fill: white;");
            closeButton.setOnAction(e -> detailStage.close());
            buttonPanel.getChildren().add(closeButton);

            ScrollPane scrollPane = new ScrollPane();
            VBox contentBox = new VBox(15);
            contentBox.getChildren().addAll(summaryBox, taskBox);
            scrollPane.setContent(contentBox);
            scrollPane.setFitToWidth(true);

            mainLayout.getChildren().addAll(titleLabel, scrollPane, buttonPanel);

            Scene scene = new Scene(mainLayout);
            detailStage.setScene(scene);
            detailStage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to load detailed report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportManagersReportToCSV(List<com.doable.model.ManagerReport> reports) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Report as CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
            fileChooser.setInitialFileName("managers_report_" + System.currentTimeMillis() + ".csv");

            File file = fileChooser.showSaveDialog(new Stage());
            if (file == null) return;

            StringBuilder csv = new StringBuilder();
            csv.append("Manager,Tasks Created,Assigned,Completed,Pending,Completion %,Employees,Performance,Last Activity\n");

            for (com.doable.model.ManagerReport report : reports) {
                csv.append(report.getManagerName()).append(",");
                csv.append(report.getTotalTasksCreated()).append(",");
                csv.append(report.getTotalTasksAssigned()).append(",");
                csv.append(report.getTotalAssignmentsCompleted()).append(",");
                csv.append(report.getTotalAssignmentsPending()).append(",");
                csv.append(String.format("%.1f", report.getOverallCompletionPercentage())).append(",");
                csv.append(report.getTotalEmployeesManaged()).append(",");
                csv.append(report.getPerformanceStatus()).append(",");
                csv.append(report.getFormattedLastActivityDate()).append("\n");
            }

            Files.writeString(file.toPath(), csv.toString());
            showAlert("Success", "Report exported to " + file.getName());
        } catch (Exception e) {
            showAlert("Error", "Failed to export report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportManagersReportToPDF(List<com.doable.model.ManagerReport> reports) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Report as PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
            fileChooser.setInitialFileName("managers_report_" + System.currentTimeMillis() + ".pdf");

            File file = fileChooser.showSaveDialog(new Stage());
            if (file == null) return;

            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(file);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
            document.setMargins(20, 20, 20, 20);

            // Title
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph("Managers Performance Report");
            title.setFontSize(24).setBold().setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(title);

            // Generated date info
            com.itextpdf.layout.element.Paragraph info = new com.itextpdf.layout.element.Paragraph(
                "Generated: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())
            );
            info.setFontSize(11).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER).setMarginBottom(15);
            document.add(info);

            // Managers Table
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(9);
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
            
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Manager")).setBold());
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Created")).setBold());
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Assigned")).setBold());
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Completed")).setBold());
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Pending")).setBold());
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Completion %")).setBold());
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Employees")).setBold());
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Performance")).setBold());
            table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph("Last Activity")).setBold());

            for (com.doable.model.ManagerReport report : reports) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getManagerName())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(report.getTotalTasksCreated()))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(report.getTotalTasksAssigned()))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(report.getTotalAssignmentsCompleted()))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(report.getTotalAssignmentsPending()))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getFormattedCompletionPercentage())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(report.getTotalEmployeesManaged()))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getPerformanceStatus())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(report.getFormattedLastActivityDate())));
            }

            document.add(table);
            document.close();
            showAlert("Success", "Report exported to " + file.getName());
        } catch (Exception e) {
            showAlert("Error", "Failed to export PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditManager(ManagerRow managerRow) {
        // Get the manager from database
        User manager = UserDao.findById(managerRow.id);
        if (manager == null) {
            showAlert("Error", "Manager not found");
            return;
        }

        // Create edit dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Manager");
        dialog.setHeaderText("Edit Manager Information");

        // Create form fields
        TextField usernameField = new TextField(manager.getUsername());
        usernameField.setPromptText("Username");
        usernameField.setEditable(false); // Username typically shouldn't be changed

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password (leave blank to keep current)");

        TextField emailField = new TextField(manager.getEmail());
        emailField.setPromptText("Email");

        TextField phoneField = new TextField(manager.getPhoneNumber());
        phoneField.setPromptText("Phone");

        TextField departmentField = new TextField(manager.getDepartment());
        departmentField.setPromptText("Department");

        // Create layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Department:"), 0, 4);
        grid.add(departmentField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Handle OK button
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Validate inputs
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String department = departmentField.getText().trim();

            if (email.isEmpty() || phone.isEmpty() || department.isEmpty()) {
                showAlert("Validation Error", "All fields are required");
                return;
            }

            if (!email.contains("@") || email.length() < 5) {
                showAlert("Validation Error", "Invalid email format");
                return;
            }

            // Update manager
            manager.setEmail(email);
            manager.setPhoneNumber(phone);
            manager.setDepartment(department);

            // Update password if provided
            String newPassword = passwordField.getText();
            if (!newPassword.isEmpty()) {
                manager.setPassword(newPassword);
            }

            try {
                UserDao.updateUser(manager);
                showAlert("Success", "Manager updated successfully");
                loadManagers();
            } catch (Exception e) {
                showAlert("Error", "Failed to update manager: " + e.getMessage());
            }
        }
    }

    private void handleDeleteManager(ManagerRow managerRow) {
        // Confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Manager");
        confirm.setHeaderText("Delete Manager");
        confirm.setContentText("Are you sure you want to delete manager '" + managerRow.username + "'? " +
                "This will NOT delete their employees.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                UserDao.deleteUser(managerRow.id);
                showAlert("Success", "Manager deleted successfully");
                loadManagers();
            } catch (Exception e) {
                showAlert("Error", "Failed to delete manager: " + e.getMessage());
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

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
            loadManagers();
        }
    }

    // Task and Category Management for Admin (Boss)
    public void createTask(String title, String description, long userId) {
        try {
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setUserId(userId);
            task.setCreatedBy(currentUser.getId());
            task.setCompleted(false);
            task.setRepeatRule("NONE");
            
            TaskDao taskDao = new TaskDao();
            taskDao.save(task);
            showAlert("Success", "Task created successfully!");
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

    // Inner class for table row
    public static class ManagerRow {
        public long id;
        public String username;
        public String email;
        public String phone;
        public String department;
        public String created;

        public ManagerRow(long id, String username, String email, String phone, String department, String created) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.department = department;
            this.created = created;
        }
    }
}
