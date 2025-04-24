package org.cbioportal.application.file.model;

import java.util.SequencedMap;

public class CnaSegMetadata implements GeneticDatatypeMetadata {
    private String cancerStudyIdentifier;
    private String geneticAlterationType;
    private String datatype;
    private String description;
    private String referenceGenomeId;

    @Override
    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceGenomeId() {
        return referenceGenomeId;
    }

    public void setReferenceGenomeId(String referenceGenomeId) {
        this.referenceGenomeId = referenceGenomeId;
    }

    @Override
    public SequencedMap<String, String> toMetadataKeyValues() {
        var metadata = GeneticDatatypeMetadata.super.toMetadataKeyValues();
        metadata.put("description", getDescription());
        metadata.put("reference_genome_id", getReferenceGenomeId());
        return metadata;
    }
}
