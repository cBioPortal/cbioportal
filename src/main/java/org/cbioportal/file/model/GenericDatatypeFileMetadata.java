package org.cbioportal.file.model;

public interface GenericDatatypeFileMetadata extends StudyRelated {
    String geneticAlterationType();
    String datatype();
    String dataFilename();
}
