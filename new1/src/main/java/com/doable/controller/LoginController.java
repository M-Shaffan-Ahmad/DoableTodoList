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
import com.doable.model.UserRole;
import com.doable.dao.UserDao;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private static Object currentUser;  // Can be Admin, Manager, or Employee
    private static String currentRole;

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

        System.out.println("DEBUG: Login attempt for username: " + username);
        
        Object user = attemptLogin(username, password);
        if (user != null) {
            currentUser = user;
            errorLabel.setText("");
            openMainWindow(user);
        } else {
            errorLabel.setText("Invalid username or password");
            passwordField.clear();
        }
    }

    private Object attemptLogin(String username, String password) {
        // Use unified UserDao to authenticate against users table
        User user = UserDao.authenticate(username, password);
        if (user != null) {
            System.out.println("DEBUG: User authenticated - " + user.getUsername() + " (Role: " + user.getRole() + ")");
            currentRole = user.getRole().name();
            return user;
        }
        
        System.out.println("DEBUG: Authentication failed for username: " + username);
        return null;
    }

    private void openMainWindow(Object user) {
        try {
            // Cast to unified User object
            User appUser = (User) user;
            Parent root = null;
            Object controller = null;
            
            System.out.println("DEBUG openMainWindow: User role = " + appUser.getRole());
            
            switch (appUser.getRole()) {
                case ADMIN:
                    System.out.println("DEBUG: Loading admin dashboard");
                    FXMLLoader adminLoader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
                    root = adminLoader.load();
                    controller = adminLoader.getController();
                    if (controller instanceof AdminDashboardController) {
                        ((AdminDashboardController) controller).setCurrentUser(appUser);
                    }
                    break;
                case MANAGER:
                    System.out.println("DEBUG: Loading manager dashboard");
                    FXMLLoader managerLoader = new FXMLLoader(getClass().getResource("/fxml/manager_dashboard.fxml"));
                    root = managerLoader.load();
                    controller = managerLoader.getController();
                    if (controller instanceof ManagerDashboardController) {
                        ((ManagerDashboardController) controller).setCurrentUser(appUser);
                    }
                    break;
                case EMPLOYEE:
                    System.out.println("DEBUG: Loading home/employee dashboard");
                    FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                    root = homeLoader.load();
                    controller = homeLoader.getController();
                    if (controller instanceof HomeController) {
                        ((HomeController) controller).setCurrentUser(appUser);
                    }
                    break;
            }

            if (root != null) {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root, 1000, 700));
                stage.setTitle("Doable - " + currentRole);
                stage.show();
            }
        } catch (Exception e) {
            errorLabel.setText("Error loading application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Object getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentRole() {
        return currentRole;
    }


    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}
