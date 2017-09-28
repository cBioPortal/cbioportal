package org.cbioportal.model;

import java.io.Serializable;

public class Info implements Serializable {
    
    private String portalVersion;
    private String dbVersion;

    public String getPortalVersion() {
        return portalVersion;
    }

    public void setPortalVersion(String portalVersion) {
        this.portalVersion = portalVersion;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }
}
