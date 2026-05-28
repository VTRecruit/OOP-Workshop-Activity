package com.taskmanager.controller;

import com.taskmanager.model.User;
import com.taskmanager.security.CredentialManager;
import com.taskmanager.util.DatabaseManager;
import com.taskmanager.util.SceneNavigator;
import com.taskmanager.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.PreparedStatement;

public class ProfileController {

    @FXML private Label         roleLabel;
    @FXML private TextField     usernameDisplay;
    @FXML private TextField     emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmField;
    @FXML private Label         messageLabel;
    @FXML private Label         errorLabel;

    @FXML
    public void initialize() {
        User u = SessionManager.getCurrentUser();
        roleLabel.setText("Role: " + u.getRole());
        usernameDisplay.setText(u.getUsername());
        emailField.setText(u.getEmail() == null ? "" : u.getEmail());
    }

    @FXML
    private void handleSave() {
        User u = SessionManager.getCurrentUser();
        String email      = emailField.getText().trim();
        String newPass    = newPasswordField.getText();
        String confirm    = confirmField.getText();

        if (!newPass.isEmpty()) {
            if (newPass.length() < 6)         { showError("Password must be at least 6 characters."); return; }
            if (!newPass.equals(confirm))     { showError("Passwords do not match."); return; }
        }

        try {
            if (!newPass.isEmpty()) {
                String hash = CredentialManager.hashPassword(newPass);
                try (PreparedStatement ps = DatabaseManager.getConnection()
                        .prepareStatement("UPDATE users SET password_hash=?, email=? WHERE id=?")) {
                    ps.setString(1, hash);
                    ps.setString(2, email.isEmpty() ? null : email);
                    ps.setInt(3, u.getId());
                    ps.executeUpdate();
                }
                u.setPasswordHash(hash);
            } else {
                try (PreparedStatement ps = DatabaseManager.getConnection()
                        .prepareStatement("UPDATE users SET email=? WHERE id=?")) {
                    ps.setString(1, email.isEmpty() ? null : email);
                    ps.setInt(2, u.getId());
                    ps.executeUpdate();
                }
            }
            u.setEmail(email.isEmpty() ? null : email);
            showSuccess("Profile updated successfully.");
            newPasswordField.clear();
            confirmField.clear();
        } catch (Exception e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        SceneNavigator.switchTo("/com/taskmanager/views/Dashboard.fxml", "Dashboard");
    }

    private void showError(String m) {
        errorLabel.setText(m); errorLabel.setVisible(true); errorLabel.setManaged(true);
        messageLabel.setVisible(false); messageLabel.setManaged(false);
    }
    private void showSuccess(String m) {
        messageLabel.setText(m); messageLabel.setVisible(true); messageLabel.setManaged(true);
        errorLabel.setVisible(false); errorLabel.setManaged(false);
    }
}
