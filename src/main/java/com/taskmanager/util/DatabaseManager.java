package com.taskmanager.util;

import java.io.File;
import java.sql.*;
import java.util.logging.Logger;

/**
 * Manages a single SQLite connection for the application.
 * The DB file lives in ~/.taskmanager/tasks.db so it is user-scoped.
 */
public class DatabaseManager {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_DIR  = System.getProperty("user.home") + "/.taskmanager";
    private static final String DB_PATH = DB_DIR + "/tasks.db";

    private static Connection connection;

    /** Initialise the database: create file, tables, and default admin user if needed. */
    public static void initialize() throws SQLException {
        new File(DB_DIR).mkdirs();
        connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        connection.setAutoCommit(true);
        enableWAL();
        createTables();
        LOG.info("Database initialised at " + DB_PATH);
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            LOG.warning("Error closing DB: " + e.getMessage());
        }
    }

    // ─── DDL ───────────────────────────────────────────────────

    private static void enableWAL() throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.execute("PRAGMA journal_mode=WAL");
            s.execute("PRAGMA foreign_keys=ON");
        }
    }

    private static void createTables() throws SQLException {
        try (Statement s = connection.createStatement()) {

            s.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    username     TEXT    NOT NULL UNIQUE,
                    password_hash TEXT   NOT NULL,
                    email        TEXT,
                    role         TEXT    NOT NULL DEFAULT 'user',
                    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id      INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    title        TEXT    NOT NULL,
                    description  TEXT,
                    priority     TEXT    NOT NULL DEFAULT 'Medium',
                    status       TEXT    NOT NULL DEFAULT 'Pending',
                    due_date     TEXT,
                    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    name  TEXT NOT NULL UNIQUE
                )
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS task_categories (
                    task_id     INTEGER REFERENCES tasks(id) ON DELETE CASCADE,
                    category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE,
                    PRIMARY KEY (task_id, category_id)
                )
            """);
        }
    }
}
