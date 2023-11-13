package org.cbioportal.model;

import jakarta.validation.constraints.NotNull;

public abstract class MolecularData extends UniqueKeyBase {

    @NotNull
    private String molecularProfileId;
    @NotNull
    private String sampleId;
    @NotNull
    private String patientId;
    @NotNull
    private String studyId;
    @NotNull
    private String value;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public abstract String getStableId();

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
