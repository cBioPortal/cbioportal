package org.cbioportal.domain.mutation.usecase;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.shared.MutationQueryOptions;
import org.springframework.stereotype.Service;

/**
 * Fetches the mutations of a single mutation profile for the samples of a sample list or for
 * explicit sample ids.
 */
@Service
public class FetchMutationsInSingleProfileUseCase {

  private final MutationRepository mutationRepository;
  private final MolecularProfileService molecularProfileService;
  private final SampleListRepository sampleListRepository;

  public FetchMutationsInSingleProfileUseCase(
      MutationRepository mutationRepository,
      MolecularProfileService molecularProfileService,
      SampleListRepository sampleListRepository) {
    this.mutationRepository = mutationRepository;
    this.molecularProfileService = molecularProfileService;
    this.sampleListRepository = sampleListRepository;
  }

  /**
   * @param sampleListId sample list restricting the samples, or null when {@code sampleIds} is used
   * @param sampleIds stable sample ids restricting the samples, or null when {@code sampleListId}
   *     is used
   * @param entrezGeneIds genes to fetch, or null/empty for all genes
   * @throws MolecularProfileNotFoundException if the profile does not exist or is not a mutation
   *     profile
   */
  public List<Mutation> execute(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      MutationQueryOptions mutationQueryOptions)
      throws MolecularProfileNotFoundException {

    List<String> requestedSampleIds = resolveSampleIds(molecularProfileId, sampleListId, sampleIds);
    if (requestedSampleIds.isEmpty()) {
      return Collections.emptyList();
    }
    return mutationRepository.getMutationsInMultipleMolecularProfiles(
        Collections.nCopies(requestedSampleIds.size(), molecularProfileId),
        requestedSampleIds,
        entrezGeneIds,
        mutationQueryOptions);
  }

  public MutationMeta executeMeta(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds)
      throws MolecularProfileNotFoundException {

    List<String> requestedSampleIds = resolveSampleIds(molecularProfileId, sampleListId, sampleIds);
    if (requestedSampleIds.isEmpty()) {
      MutationMeta empty = new MutationMeta();
      empty.setTotalCount(0);
      empty.setSampleCount(0);
      return empty;
    }
    return mutationRepository.getMetaMutationsInMultipleMolecularProfiles(
        Collections.nCopies(requestedSampleIds.size(), molecularProfileId),
        requestedSampleIds,
        entrezGeneIds);
  }

  /**
   * An empty result means "no samples", which must not be passed on to the repository: it treats an
   * empty sample list as "all samples of the profile".
   */
  private List<String> resolveSampleIds(
      String molecularProfileId, String sampleListId, List<String> sampleIds)
      throws MolecularProfileNotFoundException {

    MolecularProfile molecularProfile = validateMolecularProfile(molecularProfileId);
    if (sampleListId == null) {
      return sampleIds == null ? Collections.emptyList() : sampleIds;
    }
    // Stable sample ids can occur in several studies. Only a sample list of the profile's own study
    // may select samples, otherwise samples of the same name would match.
    SampleList sampleList = sampleListRepository.getSampleList(sampleListId);
    if (sampleList == null
        || !molecularProfile
            .getCancerStudyIdentifier()
            .equals(sampleList.getCancerStudyIdentifier())) {
      return Collections.emptyList();
    }
    return sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
  }

  private MolecularProfile validateMolecularProfile(String molecularProfileId)
      throws MolecularProfileNotFoundException {
    MolecularProfile molecularProfile =
        molecularProfileService.getMolecularProfile(molecularProfileId);
    MolecularAlterationType type = molecularProfile.getMolecularAlterationType();
    if (type != MolecularAlterationType.MUTATION_EXTENDED
        && type != MolecularAlterationType.MUTATION_UNCALLED) {
      throw new MolecularProfileNotFoundException(molecularProfileId);
    }
    return molecularProfile;
  }
}
