package com.doable;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.doable.db.Database;

public class MainApp extends Application {
    @Override
public void start(Stage primaryStage) throws Exception {
    Database.getInstance().init();

    // Load login screen first
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
    Parent root = loader.load();

    primaryStage.setTitle("Doable - Login");

    // Icon loading (ensure app_icon.png is in src/main/resources/)
    try {
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/app_icon.png")));
    } catch (Exception e) {
        System.out.println("Icon not found: " + e.getMessage());
    }

    primaryStage.setScene(new Scene(root, 800, 600));
    primaryStage.show();
}

    @Override
    public void stop() throws Exception {
        super.stop();
        Database.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
