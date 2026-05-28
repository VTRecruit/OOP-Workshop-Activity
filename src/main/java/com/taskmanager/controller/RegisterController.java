package com.taskmanager.controller;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) { showError("Username and password are required."); return; }
        if (username.length() < 3)                    { showError("Username must be at least 3 characters."); return; }
        if (password.length() < 6)                    { showError("Password must be at least 6 characters."); return; }
        if (!password.equals(confirm))                { showError("Passwords do not match."); return; }
        if (userDAO.usernameExists(username))         { showError("Username already taken."); return; }

        try {
            boolean ok = userDAO.createUser(username, password, email.isEmpty() ? null : email, "user");
            if (ok) {
                showSuccess("Account created! Redirecting to login…");
                javafx.animation.PauseTransition p = new javafx.animation.PauseTransition(
                    javafx.util.Duration.seconds(1.5));
                p.setOnFinished(e -> SceneNavigator.switchTo(
                    "/com/taskmanager/views/Login.fxml", "Login"));
                p.play();
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML private void goToLogin() {
        SceneNavigator.switchTo("/com/taskmanager/views/Login.fxml", "Login");
    }

    private void showError(String m) {
        errorLabel.setText(m); errorLabel.setVisible(true); errorLabel.setManaged(true);
        successLabel.setVisible(false); successLabel.setManaged(false);
    }
    private void showSuccess(String m) {
        successLabel.setText(m); successLabel.setVisible(true); successLabel.setManaged(true);
        errorLabel.setVisible(false); errorLabel.setManaged(false);
    }
}
