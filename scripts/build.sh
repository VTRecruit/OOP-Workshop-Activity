#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────
#  Manual build script (no Maven required)
#  Prerequisites:
#    • Java 17+ JDK (javac, jar)
#    • JavaFX 17 SDK   → https://gluonhq.com/products/javafx/
#    • sqlite-jdbc jar → https://github.com/xerial/sqlite-jdbc/releases
#
#  Usage:
#    1. Edit the two variables below to point at your local paths
#    2. chmod +x build.sh && ./build.sh
# ─────────────────────────────────────────────────────────────

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT="$SCRIPT_DIR"

# ── CONFIGURE THESE ─────────────────────────────────────────
# Path to your JavaFX SDK lib folder
JAVAFX_LIB="${JAVAFX_HOME:-/opt/javafx-sdk-17/lib}"

# Path to sqlite-jdbc jar
SQLITE_JAR="${SQLITE_JDBC:-$PROJECT/lib/sqlite-jdbc-3.45.1.0.jar}"
# ─────────────────────────────────────────────────────────────

SRC="$PROJECT/src/main/java"
RES="$PROJECT/src/main/resources"
BUILD="$PROJECT/build"
CLASSES="$BUILD/classes"
OUT="$PROJECT/taskmanager.jar"

echo "═══════════════════════════════════════════"
echo " TaskManager Build"
echo " JavaFX lib : $JAVAFX_LIB"
echo " SQLite jar : $SQLITE_JAR"
echo "═══════════════════════════════════════════"

# ── Validate ─────────────────────────────────────────────────
[ -d "$JAVAFX_LIB" ]  || { echo "[ERROR] JavaFX lib not found: $JAVAFX_LIB"; exit 1; }
[ -f "$SQLITE_JAR" ]  || { echo "[ERROR] SQLite JDBC jar not found: $SQLITE_JAR"; exit 1; }

# ── Compile ──────────────────────────────────────────────────
echo "[1/3] Compiling sources..."
rm -rf "$CLASSES" && mkdir -p "$CLASSES"

CP="$JAVAFX_LIB/javafx.base.jar:$JAVAFX_LIB/javafx.controls.jar:$JAVAFX_LIB/javafx.fxml.jar:$JAVAFX_LIB/javafx.graphics.jar:$SQLITE_JAR"

find "$SRC" -name "*.java" > /tmp/tm_sources.txt
javac --release 17 -cp "$CP" -d "$CLASSES" @/tmp/tm_sources.txt
echo "    Compiled $(wc -l < /tmp/tm_sources.txt) source files"

# ── Copy Resources ───────────────────────────────────────────
echo "[2/3] Copying resources..."
cp -r "$RES"/. "$CLASSES/"

# ── Package Fat JAR ──────────────────────────────────────────
echo "[3/3] Building fat JAR..."
FATDIR="$BUILD/fatjar"
rm -rf "$FATDIR" && mkdir -p "$FATDIR"

# Extract dependencies
cd "$FATDIR"
jar xf "$SQLITE_JAR"
for jfx in javafx.base javafx.controls javafx.fxml javafx.graphics; do
    [ -f "$JAVAFX_LIB/$jfx.jar" ] && jar xf "$JAVAFX_LIB/$jfx.jar"
done
cp -r "$CLASSES"/. "$FATDIR/"

# Write manifest
printf "Main-Class: com.taskmanager.MainApp\n\n" > /tmp/TM_MANIFEST.MF
jar cfm "$OUT" /tmp/TM_MANIFEST.MF -C "$FATDIR" .

echo ""
echo "═══════════════════════════════════════════"
echo " Build successful!"
echo " Output : $OUT ($(du -sh "$OUT" | cut -f1))"
echo ""
echo " Run with:"
echo "   java --add-opens java.base/java.lang=ALL-UNNAMED -jar taskmanager.jar"
echo "═══════════════════════════════════════════"
