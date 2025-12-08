package com.doable.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.doable.model.User;
import com.doable.dao.UserDao;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private static User currentUser;

    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }

        // Debug: show all users in database
        System.out.println("DEBUG: Login attempt for username: " + username);
        UserDao.getAllUsers();

        User user = UserDao.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            errorLabel.setText("");
            openMainWindow(user);
        } else {
            errorLabel.setText("Invalid username or password");
            passwordField.clear();
        }
    }

    private void openMainWindow(User user) {
        try {
            // Load appropriate controller based on user role
            Parent root = null;
            Object controller = null;

            switch (user.getRole()) {
                case ADMIN:
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
                    root = loader.load();
                    controller = loader.getController();
                    if (controller instanceof AdminDashboardController) {
                        ((AdminDashboardController) controller).setCurrentUser(user);
                    }
                    break;
                case MANAGER:
                    FXMLLoader managerLoader = new FXMLLoader(getClass().getResource("/fxml/manager_dashboard.fxml"));
                    root = managerLoader.load();
                    controller = managerLoader.getController();
                    if (controller instanceof ManagerDashboardController) {
                        ((ManagerDashboardController) controller).setCurrentUser(user);
                    }
                    break;
                case EMPLOYEE:
                    FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                    root = homeLoader.load();
                    controller = homeLoader.getController();
                    if (controller instanceof HomeController) {
                        ((HomeController) controller).setCurrentUser(user);
                    }
                    break;
            }

            if (root != null) {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root, 1000, 700));
                stage.setTitle("Doable - " + user.getRole().getDisplayName());
                stage.show();
            }
        } catch (Exception e) {
            errorLabel.setText("Error loading application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}
