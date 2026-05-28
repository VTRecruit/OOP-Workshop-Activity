package com.taskmanager.controller;

import com.taskmanager.dao.TaskDAO;
import com.taskmanager.model.Task;
import com.taskmanager.util.SceneNavigator;
import com.taskmanager.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    @FXML private Label        welcomeLabel;
    @FXML private Label        totalLabel;
    @FXML private Label        pendingLabel;
    @FXML private Label        progressLabel;
    @FXML private Label        doneLabel;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String>  colTitle;
    @FXML private TableColumn<Task, String>  colPriority;
    @FXML private TableColumn<Task, String>  colStatus;
    @FXML private TableColumn<Task, String>  colDue;
    @FXML private TableColumn<Task, String>  colCreated;
    @FXML private TableColumn<Task, Void>    colActions;
    @FXML private TextField    searchField;
    @FXML private ComboBox<String> priorityFilter;

    private final TaskDAO taskDAO = new TaskDAO();
    private ObservableList<Task> masterList;
    private FilteredList<Task>   filteredList;
    private String activeStatusFilter = null;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Hello, " + SessionManager.getCurrentUser().getUsername() + " 👋");

        priorityFilter.setItems(FXCollections.observableArrayList(
            "All", "High", "Medium", "Low"));
        priorityFilter.setValue("All");
        priorityFilter.setOnAction(e -> applyFilters());

        setupColumns();
        loadTasks();
    }

    private void setupColumns() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Priority badge
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colPriority.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                String color = switch (v) {
                    case "High"   -> "#ef4444";
                    case "Medium" -> "#f59e0b";
                    case "Low"    -> "#10b981";
                    default       -> "#6b7280";
                };
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        // Status badge
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                String color = switch (v) {
                    case "Completed"   -> "#10b981";
                    case "In Progress" -> "#3b82f6";
                    case "Pending"     -> "#f59e0b";
                    default            -> "#6b7280";
                };
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        // Action buttons
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("btn-xs-outline");
                deleteBtn.getStyleClass().add("btn-xs-danger");
                editBtn.setOnAction(e -> openEditTask(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadTasks() {
        try {
            int uid = SessionManager.getCurrentUser().getId();
            List<Task> tasks = taskDAO.getTasksByUser(uid);
            masterList  = FXCollections.observableArrayList(tasks);
            filteredList = new FilteredList<>(masterList, p -> true);
            taskTable.setItems(filteredList);
            refreshStats();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load tasks: " + e.getMessage());
        }
    }

    private void refreshStats() {
        int uid = SessionManager.getCurrentUser().getId();
        try {
            long total   = masterList.size();
            long pending = taskDAO.countByUserAndStatus(uid, "Pending");
            long inProg  = taskDAO.countByUserAndStatus(uid, "In Progress");
            long done    = taskDAO.countByUserAndStatus(uid, "Completed");
            totalLabel.setText("📋 Total: " + total);
            pendingLabel.setText("⏳ Pending: " + pending);
            progressLabel.setText("🔄 In Progress: " + inProg);
            doneLabel.setText("✅ Completed: " + done);
        } catch (SQLException ignored) {}
    }

    // ── Filters ───────────────────────────────────

    @FXML void filterAll()        { activeStatusFilter = null;          applyFilters(); }
    @FXML void filterPending()    { activeStatusFilter = "Pending";     applyFilters(); }
    @FXML void filterInProgress() { activeStatusFilter = "In Progress"; applyFilters(); }
    @FXML void filterCompleted()  { activeStatusFilter = "Completed";   applyFilters(); }

    @FXML void handleSearch()     { applyFilters(); }

    private void applyFilters() {
        String search   = searchField.getText().toLowerCase();
        String priority = priorityFilter.getValue();

        filteredList.setPredicate(t -> {
            boolean matchStatus   = activeStatusFilter == null || t.getStatus().equals(activeStatusFilter);
            boolean matchSearch   = search.isEmpty()
                || t.getTitle().toLowerCase().contains(search)
                || (t.getDescription() != null && t.getDescription().toLowerCase().contains(search));
            boolean matchPriority = priority == null || priority.equals("All") || t.getPriority().equals(priority);
            return matchStatus && matchSearch && matchPriority;
        });
    }

    // ── Task CRUD ─────────────────────────────────

    @FXML
    private void openNewTask() { openTaskForm(null); }

    private void openEditTask(Task task) { openTaskForm(task); }

    private void openTaskForm(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/taskmanager/views/TaskForm.fxml"));
            Parent root = loader.load();
            TaskFormController ctrl = loader.getController();
            ctrl.setTask(task);
            ctrl.setOnSaved(this::loadTasks);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(SceneNavigator.getPrimaryStage());
            stage.setTitle(task == null ? "New Task" : "Edit Task");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/com/taskmanager/styles/app.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Cannot open task form: " + e.getMessage());
        }
    }

    private void confirmDelete(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Delete \"" + task.getTitle() + "\"?");
        alert.setContentText("This action cannot be undone.");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    taskDAO.deleteTask(task.getId());
                    loadTasks();
                } catch (SQLException e) {
                    showAlert("Error", "Delete failed: " + e.getMessage());
                }
            }
        });
    }

    // ── Navigation ────────────────────────────────

    @FXML private void goToProfile() {
        SceneNavigator.switchTo("/com/taskmanager/views/Profile.fxml", "Profile");
    }

    @FXML private void handleLogout() {
        SessionManager.logout();
        SceneNavigator.switchTo("/com/taskmanager/views/Login.fxml", "Login");
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
