#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
#  TaskManager — Linux / macOS Launcher
#  Requires: Java 17+  (install via sdkman, brew, or package manager)
# ─────────────────────────────────────────────────────────────

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$SCRIPT_DIR/../taskmanager.jar"

# ── Java check ───────────────────────────────────────────────
if ! command -v java &>/dev/null; then
    echo "[ERROR] Java not found. Install Java 17+:"
    echo "  Linux : sudo apt install openjdk-17-jdk"
    echo "  macOS : brew install openjdk@17"
    echo "  Any   : https://adoptium.net/"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [ "$JAVA_VER" -lt 17 ] 2>/dev/null; then
    echo "[WARN] Java $JAVA_VER detected; Java 17+ is recommended."
fi

# ── Launch ───────────────────────────────────────────────────
echo "Starting TaskManager..."
java \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  -jar "$JAR" "$@"
