package org.cbioportal.application.rest.mapper;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.cbioportal.application.rest.response.SampleClinicalDataCollectionDTO;
import org.cbioportal.legacy.model.SampleClinicalDataCollection;

public final class SampleClinicalDataCollectionMapper {
  private SampleClinicalDataCollectionMapper() {}

  public static SampleClinicalDataCollectionDTO toDto(SampleClinicalDataCollection collection) {
    if (collection == null || collection.getByUniqueSampleKey() == null) {
      return new SampleClinicalDataCollectionDTO(Collections.emptyMap());
    }

    Map<String, java.util.List<org.cbioportal.application.rest.response.ClinicalDataDTO>> mapped =
        collection.getByUniqueSampleKey().entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e -> LegacyClinicalDataMapper.INSTANCE.toDtosForTable(e.getValue())));

    return new SampleClinicalDataCollectionDTO(mapped);
  }
}
