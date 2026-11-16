package org.cbioportal.domain.geneset_data.repository;

import java.util.List;
import org.cbioportal.legacy.model.GenesetMolecularData;

public interface GenesetDataRepository {

  /**
   * Fetches gene set scores of a profile. A sample is only returned when it is part of the profile;
   * values are rounded like the gene-level molecular data.
   *
   * @param sampleIds stable ids of samples of the profile's study, or null for all samples of the
   *     profile
   * @param genesetIds gene sets (external ids) to fetch, or null/empty for all of them
   */
  List<GenesetMolecularData> getGenesetData(
      String molecularProfileId, List<String> sampleIds, List<String> genesetIds);
}
