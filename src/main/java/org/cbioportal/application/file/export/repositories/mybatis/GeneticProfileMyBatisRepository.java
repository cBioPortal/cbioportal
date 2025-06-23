package org.cbioportal.application.file.export.repositories.mybatis;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.repositories.GeneticProfileRepository;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

public class GeneticProfileMyBatisRepository implements GeneticProfileRepository {

  private GeneticProfileMapper mapper;

  public GeneticProfileMyBatisRepository(GeneticProfileMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<GeneticProfileDatatypeMetadata> getGeneticProfiles(
      String studyId, Set<String> sampleIds, String geneticAlterationType, String datatype) {
    return mapper.getGeneticProfiles(studyId, sampleIds, geneticAlterationType, datatype);
  }
}
