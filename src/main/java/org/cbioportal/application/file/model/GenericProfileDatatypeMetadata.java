package org.cbioportal.application.file.model;

import java.util.Optional;

public class GenericProfileDatatypeMetadata implements GenericDatatypeMetadata {
    private String stableId;
    private String geneticAlterationType;
    private String datatype;
    private String cancerStudyIdentifier;
    private String dataFilename;
    private String profileName;
    private String profileDescription;
    private String genePanel;
    private Boolean showProfileInAnalysisTab;

    public GenericProfileDatatypeMetadata() {
    }

    public GenericProfileDatatypeMetadata(String stableId, String geneticAlterationType, String datatype, String cancerStudyIdentifier, String dataFilename, String profileName, String profileDescription, String genePanel, Boolean showProfileInAnalysisTab) {
        this.stableId = stableId;
        this.geneticAlterationType = geneticAlterationType;
        this.datatype = datatype;
        this.cancerStudyIdentifier = cancerStudyIdentifier;
        this.dataFilename = dataFilename;
        this.profileName = profileName;
        this.profileDescription = profileDescription;
        this.genePanel = genePanel;
        this.showProfileInAnalysisTab = showProfileInAnalysisTab;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    @Override
    public String getGeneticAlterationType() {
        return geneticAlterationType;
    }

    public void setGeneticAlterationType(String geneticAlterationType) {
        this.geneticAlterationType = geneticAlterationType;
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    @Override
    public String getDataFilename() {
        return dataFilename;
    }

    public void setDataFilename(String dataFilename) {
        this.dataFilename = dataFilename;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public String getGenePanel() {
        return genePanel;
    }

    public void setGenePanel(String genePanel) {
        this.genePanel = genePanel;
    }

    public Boolean getShowProfileInAnalysisTab() {
        return showProfileInAnalysisTab;
    }

    public void setShowProfileInAnalysisTab(Boolean showProfileInAnalysisTab) {
        this.showProfileInAnalysisTab = showProfileInAnalysisTab;
    }
}