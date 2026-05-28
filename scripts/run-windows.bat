@echo off
REM ─────────────────────────────────────────────
REM  TaskManager — Windows Launcher
REM  Requires: Java 17+ on PATH
REM ─────────────────────────────────────────────

SET SCRIPT_DIR=%~dp0

REM Check Java
java -version >nul 2>&1
IF ERRORLEVEL 1 (
    echo [ERROR] Java not found. Please install Java 17+ and add it to PATH.
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

echo Starting TaskManager...
java ^
  --add-opens java.base/java.lang=ALL-UNNAMED ^
  --add-opens java.base/java.nio=ALL-UNNAMED ^
  -jar "%SCRIPT_DIR%taskmanager.jar"

IF ERRORLEVEL 1 (
    echo.
    echo [ERROR] Application exited with an error.
    echo If you see "JavaFX runtime components are missing", install JavaFX 17+:
    echo   https://gluonhq.com/products/javafx/
    echo Then run: java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar taskmanager.jar
    pause
)
