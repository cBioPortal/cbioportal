package org.cbioportal.model;

import jakarta.validation.constraints.NotNull;

public class Alteration extends UniqueKeyBase {

    @NotNull
    private String molecularProfileId;
    @NotNull
    private String sampleId;
    @NotNull
    private String patientId;
    @NotNull
    private Integer entrezGeneId;
    private Gene gene;
    @NotNull
    private String studyId;
    private String driverFilter;
    private String driverFilterAnnotation;
    private String driverTiersFilter;
    private String driverTiersFilterAnnotation;

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

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getDriverFilter() {
        return driverFilter;
    }

    public void setDriverFilter(String driverFilter) {
        this.driverFilter = driverFilter;
    }

    public String getDriverFilterAnnotation() {
        return driverFilterAnnotation;
    }

    public void setDriverFilterAnnotation(String driverFilterAnnotation) {
        this.driverFilterAnnotation = driverFilterAnnotation;
    }

    public String getDriverTiersFilter() {
        return driverTiersFilter;
    }

    public void setDriverTiersFilter(String driverTiersFilter) {
        this.driverTiersFilter = driverTiersFilter;
    }

    public String getDriverTiersFilterAnnotation() {
        return driverTiersFilterAnnotation;
    }

    public void setDriverTiersFilterAnnotation(String driverTiersFilterAnnotation) {
        this.driverTiersFilterAnnotation = driverTiersFilterAnnotation;
    }
}
