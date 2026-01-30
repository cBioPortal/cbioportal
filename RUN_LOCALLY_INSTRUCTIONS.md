# How to Run cBioPortal Locally (No Docker for App)

Since you want to run the application natively on your machine (to view it in the browser at `http://localhost:8080`), follow these steps.

## Prerequisites

1.  **Maven (`mvn`)**: Must be installed and in your system PATH.
2.  **Database**: You **must** have a MySQL or ClickHouse database running. The application *cannot* start without it.
    *   *Recommended*: Even if you run the app locally, running the database in Docker is the easiest way to get the correct schema.
    *   *Manual*: Install MySQL 8.0+, create a database named `cbioportal`, and import `src/main/resources/db-scripts/cgds.sql`.

## 1. Setup the Database (Crucial Step)

If you have Docker installed (but only want to use it for the DB), run the database like this:
*(You may need the separate `cbioportal-docker-compose` repository for the easiest setup, or run a generic MySQL container)*.

## 2. Compile the Backend

Open your terminal in the `cbioportal` directory and run:

```powershell
mvn clean install -DskipTests
```

## 3. Run the Application

This command starts the web server on your local machine:

```powershell
mvn spring-boot:run -Dspring.datasource.url=jdbc:mysql://localhost:3306/cbioportal -Dspring.datasource.username=root -Dspring.datasource.password=your_password
```

*Replace `your_password` with your actual database password.*

## 4. Access in Browser

Once the logs say `Started PortalApplication`, open:
**http://localhost:8080**

---

## ⚠️ Troubleshooting: Maven Missing

I noticed `mvn` is currently not found in your terminal.
**Action Required**:
1.  Locate your Maven `bin` folder (e.g., `C:\Program Files\Apache\maven\bin`).
2.  Add it to your **System Environment Variables** -> **Path**.
3.  Restart your terminal/VS Code-server.
