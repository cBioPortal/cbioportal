package org.cbioportal.web.parameter;

import java.io.Serializable;

public class ClinicalDataIdentifier implements Serializable {

    private String entityId;
    private String studyId;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }
}
