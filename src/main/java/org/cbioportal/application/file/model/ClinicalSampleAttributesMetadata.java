package org.cbioportal.application.file.model;

public class ClinicalSampleAttributesMetadata implements GenericDatatypeMetadata {
    private String cancerStudyIdentifier;
    private String dataFilename;

    public ClinicalSampleAttributesMetadata() {
    }

    public ClinicalSampleAttributesMetadata(String cancerStudyIdentifier, String dataFilename) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
        this.dataFilename = dataFilename;
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

    @Override
    public String getGeneticAlterationType() {
        return "CLINICAL";
    }

    @Override
    public String getDatatype() {
        return "SAMPLE_ATTRIBUTES";
    }

}