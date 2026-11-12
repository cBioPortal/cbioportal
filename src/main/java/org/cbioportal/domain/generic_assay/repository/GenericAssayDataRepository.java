package org.cbioportal.domain.generic_assay.repository;

import java.util.List;
import org.cbioportal.legacy.model.GenericAssayData;

public interface GenericAssayDataRepository {

  /**
   * Fetches the non-empty values of a generic assay profile. Values are rounded like the gene-level
   * molecular data; empty values and "NA" are not returned.
   *
   * @param studyId the cancer study identifier the profile belongs to
   * @param profileType the profile type, i.e. the molecular profile id without the study prefix
   * @param sampleIds stable ids of samples of the profile's study, or null for all samples
   * @param genericAssayStableIds entities to fetch, or null/empty for all entities
   */
  List<GenericAssayData> getGenericAssayData(
      String studyId,
      String molecularProfileId,
      String profileType,
      List<String> sampleIds,
      List<String> genericAssayStableIds);
}
