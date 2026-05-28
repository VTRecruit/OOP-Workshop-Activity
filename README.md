# TaskManager

## Overview
TaskManager is a desktop Java application built with **JavaFX** and **SQLite**. Designed with a clean Object-Oriented Programming (OOP) architecture, it utilizes the DAO (Data Access Object) pattern to separate database operations from the user interface, ensuring a scalable and maintainable codebase.

### Key Features
* **Automated Database Setup:** Automatically initializes the SQLite database on the first run.
* **User Authentication:** Secure login system with credential management. 
* **Automated Admin Seeding:** Generates a default administrator account automatically if no users exist in the system.
* **Task Management:** Create, track, and manage tasks with comprehensive details including title, description, priority, status, and due date.
* **Profile Management:** Allows users to update their credentials securely from within the application.

---

## How to Use

### Prerequisites
To run this project, you will need the following installed on your machine:
* **Java Development Kit (JDK):** Version 21 (or compatible)
* **Maven:** For dependency and build management
* **JavaFX SDK:** Only required if you are configuring your IDE's launch files manually instead of using Maven.

### Getting Started

**1. Open the Project**
Open the project folder in your preferred IDE (such as Visual Studio Code or IntelliJ IDEA).

**2. Run the Application**
The easiest and most reliable way to launch the application is by using Maven. Open your terminal in the project root directory (where your `pom.xml` is located) and run the following command:

```bash
mvn clean javafx:run
