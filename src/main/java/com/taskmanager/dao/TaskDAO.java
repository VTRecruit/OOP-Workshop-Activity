package com.taskmanager.dao;

import com.taskmanager.model.Task;
import com.taskmanager.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public void createTask(Task task) throws SQLException {
        String sql = """
            INSERT INTO tasks (user_id, title, description, priority, status, due_date)
            VALUES (?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, task.getUserId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getPriority());
            ps.setString(5, task.getStatus());
            ps.setString(6, task.getDueDate());
            ps.executeUpdate();
        }
    }

    public void updateTask(Task task) throws SQLException {
        String sql = """
            UPDATE tasks
            SET title=?, description=?, priority=?, status=?, due_date=?,
                updated_at=CURRENT_TIMESTAMP
            WHERE id=?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority());
            ps.setString(4, task.getStatus());
            ps.setString(5, task.getDueDate());
            ps.setInt(6, task.getId());
            ps.executeUpdate();
        }
    }

    public void deleteTask(int taskId) throws SQLException {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM tasks WHERE id=?")) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    public List<Task> getTasksByUser(int userId) throws SQLException {
        String sql = """
            SELECT id, user_id, title, description, priority, status, due_date, created_at
            FROM tasks WHERE user_id=? ORDER BY created_at DESC
        """;
        List<Task> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Task> getTasksByUserAndStatus(int userId, String status) throws SQLException {
        String sql = """
            SELECT id, user_id, title, description, priority, status, due_date, created_at
            FROM tasks WHERE user_id=? AND status=? ORDER BY priority, due_date
        """;
        List<Task> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public long countByUserAndStatus(int userId, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id=? AND status=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            return ps.executeQuery().getLong(1);
        }
    }

    private Task map(ResultSet rs) throws SQLException {
        return new Task(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("priority"),
            rs.getString("status"),
            rs.getString("due_date"),
            rs.getString("created_at")
        );
    }
}
