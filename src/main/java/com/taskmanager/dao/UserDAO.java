package com.taskmanager.dao;

import com.taskmanager.model.User;
import com.taskmanager.security.CredentialManager;
import com.taskmanager.util.DatabaseManager;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger LOG = Logger.getLogger(UserDAO.class.getName());

    /** Register a new user. Password is hashed with BCrypt before storing. */
    public boolean createUser(String username, String plainPassword, String email, String role)
            throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, email, role) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, CredentialManager.hashPassword(plainPassword));
            ps.setString(3, email);
            ps.setString(4, role);
            ps.executeUpdate();
            LOG.info("User created: " + username);
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                LOG.warning("Username already exists: " + username);
                return false;
            }
            throw e;
        }
    }

    /** Attempt login. Returns the User if credentials are valid, empty otherwise. */
    public Optional<User> authenticate(String username, String plainPassword) {
        String sql = "SELECT id, username, password_hash, email, role FROM users WHERE username = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String stored = rs.getString("password_hash");
                if (CredentialManager.verifyPassword(plainPassword, stored)) {
                    User u = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        stored,
                        rs.getString("email"),
                        rs.getString("role")
                    );
                    return Optional.of(u);
                }
            }
        } catch (SQLException e) {
            LOG.severe("Auth error: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Check if any users exist (first-run detection). */
    public boolean hasUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.getInt(1) > 0;
        }
    }

    public boolean usernameExists(String username) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT 1 FROM users WHERE username=?")) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }
}
