package org.cbioportal.web.parameter;

import java.io.Serializable;

public class PatientIdentifier implements Serializable {

    private String patientId;
    private String studyId;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }
}
