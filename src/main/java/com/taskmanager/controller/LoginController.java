package com.taskmanager.controller;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.User;
import com.taskmanager.util.SceneNavigator;
import com.taskmanager.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Label         statusLabel;
    @FXML private Button        loginButton;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Allow Enter key to submit
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Signing in…");

        Optional<User> user = userDAO.authenticate(username, password);
        if (user.isPresent()) {
            SessionManager.setCurrentUser(user.get());
            SceneNavigator.switchTo("/com/taskmanager/views/Dashboard.fxml", "Dashboard");
        } else {
            showError("Invalid username or password.");
            passwordField.clear();
            loginButton.setDisable(false);
            loginButton.setText("Sign In");
        }
    }

    @FXML
    private void goToRegister() {
        SceneNavigator.switchTo("/com/taskmanager/views/Register.fxml", "Register");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
