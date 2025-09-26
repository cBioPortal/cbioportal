package org.cbioportal.domain.mutation.repository;

import java.util.List;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.shared.MutationSearchCriteria;

/** Repository interface for accessing mutation data. */
public interface MutationRepository {
  /**
   * @param molecularProfileIds
   * @param sampleIds
   * @param entrezGeneIds
   * @param mutationSearchCriteria
   * @return mutation
   */
  List<Mutation> getMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      MutationSearchCriteria mutationSearchCriteria);

  MutationMeta getMetaMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds);
}
