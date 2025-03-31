package org.cbioportal.application.file.model;

public interface GenericDatatypeMetadata extends StudyRelatedMetadata {
    String getGeneticAlterationType();

    String getDatatype();

    String getDataFilename();
}
