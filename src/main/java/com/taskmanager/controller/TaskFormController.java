package com.taskmanager.controller;

import com.taskmanager.dao.TaskDAO;
import com.taskmanager.model.Task;
import com.taskmanager.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class TaskFormController {

    @FXML private Label      formTitle;
    @FXML private TextField  titleField;
    @FXML private TextArea   descField;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField  dueDateField;
    @FXML private Label      errorLabel;

    private Task task;
    private Runnable onSaved;
    private final TaskDAO taskDAO = new TaskDAO();

    @FXML
    public void initialize() {
        priorityCombo.setItems(FXCollections.observableArrayList("High", "Medium", "Low"));
        statusCombo.setItems(FXCollections.observableArrayList("Pending", "In Progress", "Completed"));
        priorityCombo.setValue("Medium");
        statusCombo.setValue("Pending");
    }

    /** Called after FXML load to pass the task (null = new). */
    public void setTask(Task t) {
        this.task = t;
        if (t != null) {
            formTitle.setText("Edit Task");
            titleField.setText(t.getTitle());
            descField.setText(t.getDescription());
            priorityCombo.setValue(t.getPriority());
            statusCombo.setValue(t.getStatus());
            dueDateField.setText(t.getDueDate() == null ? "" : t.getDueDate());
        }
    }

    public void setOnSaved(Runnable callback) { this.onSaved = callback; }

    @FXML
    private void handleSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) { showError("Title is required."); return; }

        String dueDate = dueDateField.getText().trim();
        if (!dueDate.isEmpty() && !dueDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showError("Due date must be YYYY-MM-DD format."); return;
        }

        try {
            if (task == null) {
                Task t = new Task();
                t.setUserId(SessionManager.getCurrentUser().getId());
                t.setTitle(title);
                t.setDescription(descField.getText().trim());
                t.setPriority(priorityCombo.getValue());
                t.setStatus(statusCombo.getValue());
                t.setDueDate(dueDate.isEmpty() ? null : dueDate);
                taskDAO.createTask(t);
            } else {
                task.setTitle(title);
                task.setDescription(descField.getText().trim());
                task.setPriority(priorityCombo.getValue());
                task.setStatus(statusCombo.getValue());
                task.setDueDate(dueDate.isEmpty() ? null : dueDate);
                taskDAO.updateTask(task);
            }
            if (onSaved != null) onSaved.run();
            close();
        } catch (Exception e) {
            showError("Save failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() { close(); }

    private void close() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
