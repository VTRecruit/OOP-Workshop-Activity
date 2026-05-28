package com.taskmanager;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.util.DatabaseManager;
import com.taskmanager.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * JavaFX entry point.
 * Boot sequence:
 *  1. Initialise SQLite database
 *  2. Seed a default admin if no users exist
 *  3. Show Login scene
 */
public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. DB
        DatabaseManager.initialize();

        // 2. Seed admin on first run
        UserDAO userDAO = new UserDAO();
        if (!userDAO.hasUsers()) {
            userDAO.createUser("admin", "admin123", "admin@taskmanager.local", "admin");
            LOG.info("Default admin created  →  username: admin  |  password: admin123");
            System.out.println("══════════════════════════════════════════════");
            System.out.println(" First run detected — default admin created:");
            System.out.println("   Username : admin");
            System.out.println("   Password : admin123");
            System.out.println(" Change this password from the Profile screen!");
            System.out.println("══════════════════════════════════════════════");
        }

        // 3. Stage setup
        primaryStage.setTitle("TaskManager");
        primaryStage.setMinWidth(420);
        primaryStage.setMinHeight(480);
        primaryStage.setOnCloseRequest(e -> DatabaseManager.close());

        SceneNavigator.setPrimaryStage(primaryStage);
        SceneNavigator.switchTo("/com/taskmanager/views/Login.fxml", "Login");
    }

    @Override
    public void stop() {
        DatabaseManager.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
