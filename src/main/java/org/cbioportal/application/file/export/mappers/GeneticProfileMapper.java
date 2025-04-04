package org.cbioportal.application.file.export.mappers;

import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

import java.util.List;

public interface GeneticProfileMapper {
    List<GeneticProfileDatatypeMetadata> getGeneticProfiles(String studyId, String geneticAlterationType, String datatype);
}
