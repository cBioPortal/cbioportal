package org.cbioportal.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class Info implements Serializable {
    
    @NotNull
    private String portalVersion;
    @NotNull
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
