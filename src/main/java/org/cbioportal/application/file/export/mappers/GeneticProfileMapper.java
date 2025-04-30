package org.cbioportal.application.file.export.mappers;

import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

import java.util.List;
import java.util.Set;

public interface GeneticProfileMapper {
    List<GeneticProfileDatatypeMetadata> getGeneticProfiles(String studyId, Set<String> sampleIds, String geneticAlterationType, String datatype);
}
