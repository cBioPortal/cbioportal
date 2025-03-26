package org.cbioportal.application.file.model;

public class GeneticProfile {
    private String stableId;
    private String name;
    private String description;
    private String geneticAlterationType;
    private String genericAssayType;
    private String datatype;
    private Boolean showProfileInAnalysisTab;
    private Float pivotThreshold;
    private String sortOrder;
    private Boolean patientLevel;

    public GeneticProfile() {
    }

    public GeneticProfile(String stableId, String name, String description, String geneticAlterationType, String genericAssayType, String datatype, Boolean showProfileInAnalysisTab, Float pivotThreshold, String sortOrder, Boolean patientLevel) {
        this.stableId = stableId;
        this.name = name;
        this.description = description;
        this.geneticAlterationType = geneticAlterationType;
        this.genericAssayType = genericAssayType;
        this.datatype = datatype;
        this.showProfileInAnalysisTab = showProfileInAnalysisTab;
        this.pivotThreshold = pivotThreshold;
        this.sortOrder = sortOrder;
        this.patientLevel = patientLevel;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGeneticAlterationType() {
        return geneticAlterationType;
    }

    public void setGeneticAlterationType(String geneticAlterationType) {
        this.geneticAlterationType = geneticAlterationType;
    }

    public String getGenericAssayType() {
        return genericAssayType;
    }

    public void setGenericAssayType(String genericAssayType) {
        this.genericAssayType = genericAssayType;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public Boolean getShowProfileInAnalysisTab() {
        return showProfileInAnalysisTab;
    }

    public void setShowProfileInAnalysisTab(Boolean showProfileInAnalysisTab) {
        this.showProfileInAnalysisTab = showProfileInAnalysisTab;
    }

    public Float getPivotThreshold() {
        return pivotThreshold;
    }

    public void setPivotThreshold(Float pivotThreshold) {
        this.pivotThreshold = pivotThreshold;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getPatientLevel() {
        return patientLevel;
    }

    public void setPatientLevel(Boolean patientLevel) {
        this.patientLevel = patientLevel;
    }
}
