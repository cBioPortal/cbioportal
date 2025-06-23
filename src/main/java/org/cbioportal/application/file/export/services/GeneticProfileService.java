package org.cbioportal.application.file.export.services;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.repositories.GeneticProfileRepository;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

public class GeneticProfileService {

  private final GeneticProfileRepository geneticProfileRepository;

  public GeneticProfileService(GeneticProfileRepository geneticProfileRepository) {
    this.geneticProfileRepository = geneticProfileRepository;
  }

  public List<GeneticProfileDatatypeMetadata> getGeneticProfiles(
      String studyId, Set<String> sampleIds, String geneticAlterationType, String datatype) {
    return geneticProfileRepository.getGeneticProfiles(
        studyId, sampleIds, geneticAlterationType, datatype);
  }
}
