package org.cbioportal.file.model;

public interface GenericDatatypeMetadata extends StudyRelatedMetadata {
    String geneticAlterationType();
    String datatype();
    String dataFilename();
}
