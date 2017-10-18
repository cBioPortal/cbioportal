package org.cbioportal.model;

public class MutationSpectrum extends UniqueKeyBase {
    
    private String molecularProfileId;
    private String sampleId;
    private String patientId;
    private String studyId;
    private Integer ctoA;
    private Integer ctoG;
    private Integer ctoT;
    private Integer ttoA;
    private Integer ttoC;
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
