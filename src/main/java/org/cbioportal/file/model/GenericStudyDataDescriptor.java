package org.cbioportal.file.model;

public interface GenericStudyDataDescriptor extends StudyRelated {
    String geneticAlterationType();
    String datatype();
    String dataFilename();
}
