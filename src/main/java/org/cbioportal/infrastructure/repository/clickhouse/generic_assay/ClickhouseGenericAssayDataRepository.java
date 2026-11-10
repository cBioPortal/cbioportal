package org.cbioportal.infrastructure.repository.clickhouse.generic_assay;

import java.util.List;
import org.cbioportal.domain.generic_assay.repository.GenericAssayDataRepository;
import org.cbioportal.legacy.model.GenericAssayData;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseGenericAssayDataRepository implements GenericAssayDataRepository {

  private final ClickhouseGenericAssayDataMapper mapper;

  public ClickhouseGenericAssayDataRepository(ClickhouseGenericAssayDataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<GenericAssayData> getGenericAssayData(
      String studyId,
      String molecularProfileId,
      String profileType,
      List<String> sampleIds,
      List<String> genericAssayStableIds) {
    // sample_unique_id in the derived tables is "<studyId>_<sampleId>"
    String[] sampleUniqueIds =
        sampleIds == null
            ? null
            : sampleIds.stream().map(id -> studyId + "_" + id).toArray(String[]::new);
    return mapper.getGenericAssayData(
        studyId, molecularProfileId, profileType, sampleUniqueIds, genericAssayStableIds);
  }
}
