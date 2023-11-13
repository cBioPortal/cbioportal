package org.cbioportal.model;

import jakarta.validation.constraints.NotNull;

public class MutationSpectrum extends UniqueKeyBase {
    
    @NotNull
    private String molecularProfileId;
    @NotNull
    private String sampleId;
    @NotNull
    private String patientId;
    @NotNull
    private String studyId;
    @NotNull
    private Integer ctoA;
    @NotNull
    private Integer ctoG;
    @NotNull
    private Integer ctoT;
    @NotNull
    private Integer ttoA;
    @NotNull
    private Integer ttoC;
    @NotNull
    private Integer ttoG;

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

    public Integer getCtoA() {
        return ctoA;
    }

    public void setCtoA(Integer ctoA) {
        this.ctoA = ctoA;
    }

    public Integer getCtoG() {
        return ctoG;
    }

    public void setCtoG(Integer ctoG) {
        this.ctoG = ctoG;
    }

    public Integer getCtoT() {
        return ctoT;
    }

    public void setCtoT(Integer ctoT) {
        this.ctoT = ctoT;
    }

    public Integer getTtoA() {
        return ttoA;
    }

    public void setTtoA(Integer ttoA) {
        this.ttoA = ttoA;
    }

    public Integer getTtoC() {
        return ttoC;
    }

    public void setTtoC(Integer ttoC) {
        this.ttoC = ttoC;
    }

    public Integer getTtoG() {
        return ttoG;
    }

    public void setTtoG(Integer ttoG) {
        this.ttoG = ttoG;
    }
}
