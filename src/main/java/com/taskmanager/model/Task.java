package com.taskmanager.model;

import javafx.beans.property.*;

public class Task {
    private final IntegerProperty id          = new SimpleIntegerProperty();
    private final IntegerProperty userId      = new SimpleIntegerProperty();
    private final StringProperty  title       = new SimpleStringProperty();
    private final StringProperty  description = new SimpleStringProperty();
    private final StringProperty  priority    = new SimpleStringProperty();
    private final StringProperty  status      = new SimpleStringProperty();
    private final StringProperty  dueDate     = new SimpleStringProperty();
    private final StringProperty  createdAt   = new SimpleStringProperty();

    public Task() {}

    public Task(int id, int userId, String title, String description,
                String priority, String status, String dueDate, String createdAt) {
        setId(id); setUserId(userId); setTitle(title);
        setDescription(description); setPriority(priority);
        setStatus(status); setDueDate(dueDate); setCreatedAt(createdAt);
    }

    // Properties
    public IntegerProperty idProperty()          { return id; }
    public IntegerProperty userIdProperty()      { return userId; }
    public StringProperty  titleProperty()       { return title; }
    public StringProperty  descriptionProperty() { return description; }
    public StringProperty  priorityProperty()    { return priority; }
    public StringProperty  statusProperty()      { return status; }
    public StringProperty  dueDateProperty()     { return dueDate; }
    public StringProperty  createdAtProperty()   { return createdAt; }

    // Plain getters/setters
    public int    getId()               { return id.get(); }
    public void   setId(int v)          { id.set(v); }
    public int    getUserId()           { return userId.get(); }
    public void   setUserId(int v)      { userId.set(v); }
    public String getTitle()            { return title.get(); }
    public void   setTitle(String v)    { title.set(v); }
    public String getDescription()      { return description.get(); }
    public void   setDescription(String v){ description.set(v); }
    public String getPriority()         { return priority.get(); }
    public void   setPriority(String v) { priority.set(v); }
    public String getStatus()           { return status.get(); }
    public void   setStatus(String v)   { status.set(v); }
    public String getDueDate()          { return dueDate.get(); }
    public void   setDueDate(String v)  { dueDate.set(v); }
    public String getCreatedAt()        { return createdAt.get(); }
    public void   setCreatedAt(String v){ createdAt.set(v); }
}
