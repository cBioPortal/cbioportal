package org.cbioportal.application.file.model;

public class ClinicalAttributesMetadata implements GenericDatatypeMetadata {
    private String cancerStudyIdentifier;
    private String dataFilename;

    private String geneticAlterationType;

    private String datatype;

    public ClinicalAttributesMetadata() {
    }

    public ClinicalAttributesMetadata(String cancerStudyIdentifier, String geneticAlterationType, String datatype, String dataFilename) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
        this.geneticAlterationType = geneticAlterationType;
        this.datatype = datatype;
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
        return this.geneticAlterationType;
    }

    public void setGeneticAlterationType(String geneticAlterationType) {
        this.geneticAlterationType = geneticAlterationType;
    }

    @Override
    public String getDatatype() {
        return this.datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
}