package org.cbioportal.application.file.export.mappers;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

public interface GeneticProfileMapper {
  List<GeneticProfileDatatypeMetadata> getGeneticProfiles(
      String studyId, Set<String> sampleIds, String geneticAlterationType, String datatype);
}
