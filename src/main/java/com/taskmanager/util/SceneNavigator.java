package com.taskmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;

public class SceneNavigator {

    private static final Logger LOG = Logger.getLogger(SceneNavigator.class.getName());
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) { primaryStage = stage; }
    public static Stage getPrimaryStage()            { return primaryStage; }

    public static void switchTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                SceneNavigator.class.getResource("/com/taskmanager/styles/app.css").toExternalForm());
            primaryStage.setTitle("TaskManager — " + title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            LOG.severe("Cannot load scene " + fxmlPath + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
