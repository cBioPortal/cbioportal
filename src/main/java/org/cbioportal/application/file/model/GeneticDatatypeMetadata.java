package org.cbioportal.application.file.model;

import java.util.SequencedMap;

//TODO Rename
public interface GeneticDatatypeMetadata extends StudyRelatedMetadata {
    String getGeneticAlterationType();

    String getDatatype();

    @Override
    default SequencedMap<String, String> toMetadataKeyValues() {
        var metadata = StudyRelatedMetadata.super.toMetadataKeyValues();
        metadata.put("genetic_alteration_type", getGeneticAlterationType());
        metadata.put("datatype", getDatatype());
        return metadata;
    }
}
