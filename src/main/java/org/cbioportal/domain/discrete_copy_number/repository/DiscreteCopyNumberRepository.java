package org.cbioportal.domain.discrete_copy_number.repository;

import java.util.List;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;

/** Discrete copy number events (as stored in the CNA event tables) of a molecular profile. */
public interface DiscreteCopyNumberRepository {

  /**
   * @param sampleListId sample list restricting the samples, or null
   * @param sampleIds stable sample ids restricting the samples, or null
   * @param entrezGeneIds genes to fetch, or null/empty for all genes
   * @param alterationTypes copy number alterations to fetch, e.g. -2 and 2
   * @param includeGene whether to populate the gene of each entry
   * @return the events ordered by gene, alteration and sample
   */
  List<DiscreteCopyNumberData> getDiscreteCopyNumbers(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      boolean includeGene);

  int countDiscreteCopyNumbers(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes);
}
