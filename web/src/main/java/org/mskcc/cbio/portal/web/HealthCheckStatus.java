package org.mskcc.cbio.portal.web;

import java.io.Serializable;

public class HealthCheckStatus implements Serializable {

    private Status status;
    private DatabaseHealthCheckStatus db;

    public HealthCheckStatus() {
    }

    public HealthCheckStatus(Status healthStatus, Status dbStatus) {
        this.status = healthStatus;
        db = new DatabaseHealthCheckStatus(dbStatus);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public DatabaseHealthCheckStatus getDb() {
        return db;
    }

    public void setDb(DatabaseHealthCheckStatus db) {
        this.db = db;
    }

    private class DatabaseHealthCheckStatus implements Serializable {
        private Status status;

        public DatabaseHealthCheckStatus() {
        }

        public DatabaseHealthCheckStatus(Status status) {
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }

    public enum Status {
        UP, DOWN
    }
}
