#TaskManager - Java Desktop Application

A full-featured JavaFX desktop application with multi-scene navigation,
SQLite database persistence, and industry-grade credential security -
using **only built-in Java libraries** (no external crypto dependencies).

---

## Screenshots / Scenes

| Scene | Description |
|---|---|
| **Login** | Secure sign-in with BCrypt-equivalent hashing |
| **Register** | Create account with password strength validation |
| **Dashboard** | Task table with sidebar stats, search, priority/status filters |
| **Task Form** | Modal dialog - create or edit tasks |
| **Profile** | Change email and password |

---

## Quick Start

### Prerequisites

| Requirement | Version | Download |
|---|---|---|
| Java JDK | 17 or higher | https://adoptium.net/ |
| JavaFX SDK | 17 or higher | https://gluonhq.com/products/javafx/ |
| SQLite JDBC | 3.45+ | https://github.com/xerial/sqlite-jdbc/releases |

### Option A - Maven (recommended)

```bash
# Requires internet access to Maven Central
mvn javafx:run          # run directly
mvn package             # build target/taskmanager-fat.jar
```

### Option B - Manual build (no Maven)

1. Edit `scripts/build.sh` (or `.bat`): set `JAVAFX_HOME` and `SQLITE_JDBC`
2. Run:
   ```bash
   chmod +x scripts/build.sh
   ./scripts/build.sh
   ```
3. Launch:
   ```bash
   java --add-opens java.base/java.lang=ALL-UNNAMED -jar taskmanager.jar
   ```

### Option C - Pre-built JAR

The included `taskmanager.jar` was compiled on Ubuntu 24 with OpenJDK 21 and
JavaFX 11. To run it you need JavaFX on the module path:

```bash
# Linux / macOS
java \
  --module-path /path/to/javafx-sdk/lib \
  --add-modules javafx.controls,javafx.fxml \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  -jar taskmanager.jar

# Windows
java ^
  --module-path C:\javafx-sdk\lib ^
  --add-modules javafx.controls,javafx.fxml ^
  --add-opens java.base/java.lang=ALL-UNNAMED ^
  -jar taskmanager.jar
```

---

## Default Credentials (First Run)

On first launch the app auto-creates:

```
Username : admin
Password : admin123
```

> **Change the admin password immediately** from the Profile screen.

---

## Security Design

### Password Hashing - PBKDF2WithHmacSHA256

```
Iterations : 100,000
Key length : 256 bits
Salt       : 128-bit cryptographically random (unique per user)
Storage    : Base64(salt):Base64(hash)  ←  stored in SQLite
```

Passwords are **never stored in plain text**. Even with full DB access,
an attacker faces 100k PBKDF2 iterations per password guess.

### Config File Encryption - AES-256-GCM

Any sensitive app config (e.g. DB path overrides) is stored in
`~/.taskmanager/config.enc` using:

```
Algorithm  : AES-256-GCM (authenticated encryption)
IV         : 96-bit random per write
Key        : PBKDF2-derived from machine + app seed
File perms : owner read/write only (chmod 600 equivalent)
```

### Session Security

- Sessions are **memory-only** - nothing written to disk at login
- Logout clears the in-memory session immediately
- A fresh login is required on every application start

### SQL Injection Prevention

All database access uses `PreparedStatement` with parameterised queries.
No string concatenation is used in SQL anywhere.

---

## Project Structure

```
OOP WORKSHOP/
├── taskmanager.jar                          ← Pre-built fat JAR
├── pom.xml                                  ← Maven build (Java 17 + JavaFX 17)
├── scripts/
│   ├── build.sh                             ← Manual build (Linux/macOS)
│   ├── run-unix.sh                          ← Launch (Linux/macOS)
│   └── run-windows.bat                      ← Launch (Windows)
└── src/main/
    ├── java/com/taskmanager/
    │   ├── MainApp.java                     ← Entry point
    │   ├── security/
    │   │   └── CredentialManager.java       ← PBKDF2 + AES-GCM
    │   ├── model/
    │   │   ├── User.java
    │   │   └── Task.java                    ← JavaFX ObservableProperties
    │   ├── dao/
    │   │   ├── UserDAO.java                 ← Auth + user CRUD
    │   │   └── TaskDAO.java                 ← Task CRUD + stats
    │   ├── controller/
    │   │   ├── LoginController.java
    │   │   ├── RegisterController.java
    │   │   ├── DashboardController.java     ← Main scene + filtering
    │   │   ├── TaskFormController.java      ← Modal create/edit
    │   │   └── ProfileController.java
    │   └── util/
    │       ├── DatabaseManager.java         ← SQLite init + WAL mode
    │       ├── SessionManager.java          ← In-memory only
    │       └── SceneNavigator.java          ← Scene switching
    └── resources/com/taskmanager/
        ├── views/
        │   ├── Login.fxml
        │   ├── Register.fxml
        │   ├── Dashboard.fxml
        │   ├── TaskForm.fxml
        │   └── Profile.fxml
        └── styles/
            └── app.css                      ← Full design system
```

---

## Database Schema

```sql
-- Users table (passwords hashed, never plain text)
CREATE TABLE users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT    NOT NULL UNIQUE,
    password_hash TEXT    NOT NULL,   -- PBKDF2 hash
    email         TEXT,
    role          TEXT    NOT NULL DEFAULT 'user',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tasks table
CREATE TABLE tasks (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title        TEXT    NOT NULL,
    description  TEXT,
    priority     TEXT    NOT NULL DEFAULT 'Medium',  -- High / Medium / Low
    status       TEXT    NOT NULL DEFAULT 'Pending', -- Pending / In Progress / Completed
    due_date     TEXT,                               -- YYYY-MM-DD
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

Database file location: `~/.taskmanager/tasks.db`

---

## Features

- **Multi-user** — each user sees only their own tasks
- **Live search** — filter tasks by title/description as you type
- **Priority filter** — High / Medium / Low
- **Status filter** — sidebar buttons for quick filtering
- **Stats sidebar** — live counts per status
- **Color-coded badges** — priority and status highlighted in the table
- **Modal task form** — non-blocking, returns focus to dashboard on save
- **Input validation** — all forms validate before submit
- **First-run seed** — default admin created automatically

---

## Technology Stack

| Layer | Technology |
|---|---|
| UI Framework | JavaFX 17 (FXML + CSS) |
| Database | SQLite 3 via Xerial JDBC |
| Password hashing | PBKDF2WithHmacSHA256 (javax.crypto) |
| Config encryption | AES-256-GCM (javax.crypto) |
| Build tool | Apache Maven 3 |
| Min Java version | 17 |
