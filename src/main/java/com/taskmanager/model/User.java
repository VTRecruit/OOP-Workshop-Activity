package com.taskmanager.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String username, String passwordHash, String email, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
    }

    // Getters / setters
    public int getId()                           { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getUsername()                  { return username; }
    public void setUsername(String username)    { this.username = username; }
    public String getPasswordHash()              { return passwordHash; }
    public void setPasswordHash(String h)       { this.passwordHash = h; }
    public String getEmail()                     { return email; }
    public void setEmail(String email)          { this.email = email; }
    public String getRole()                      { return role; }
    public void setRole(String role)            { this.role = role; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }

    @Override public String toString() { return username; }
}
