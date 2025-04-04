package org.cbioportal.application.file.model;

//TODO move to a more appropriate package. These are not model classes.
public class ClinicalAttributesMetadata implements GeneticDatatypeMetadata {
    private String cancerStudyIdentifier;

    private String geneticAlterationType;

    private String datatype;

    public ClinicalAttributesMetadata() {
    }

    public ClinicalAttributesMetadata(String cancerStudyIdentifier, String geneticAlterationType, String datatype) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
        this.geneticAlterationType = geneticAlterationType;
        this.datatype = datatype;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
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