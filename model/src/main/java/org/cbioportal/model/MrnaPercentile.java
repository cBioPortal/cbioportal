package org.cbioportal.model;

import java.math.BigDecimal;

public class MrnaPercentile extends UniqueKeyBase {

    private String molecularProfileId;
    private String sampleId;
    private String patientId;
    private String studyId;
    private Integer entrezGeneId;
    private BigDecimal percentile;
    private BigDecimal zScore;

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

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public BigDecimal getPercentile() {
        return percentile;
    }

    public void setPercentile(BigDecimal percentile) {
        this.percentile = percentile;
    }

    public BigDecimal getzScore() {
        return zScore;
    }

    public void setzScore(BigDecimal zScore) {
        this.zScore = zScore;
    }
}
