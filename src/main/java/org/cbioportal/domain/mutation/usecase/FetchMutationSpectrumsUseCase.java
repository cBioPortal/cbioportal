package org.cbioportal.domain.mutation.usecase;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.legacy.model.MutationSpectrum;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.springframework.stereotype.Service;

/** Fetches the mutation spectrum (substitution counts) of samples of a mutation profile. */
@Service
public class FetchMutationSpectrumsUseCase {

  private final MutationRepository mutationRepository;
  private final MolecularProfileService molecularProfileService;

  public FetchMutationSpectrumsUseCase(
      MutationRepository mutationRepository, MolecularProfileService molecularProfileService) {
    this.mutationRepository = mutationRepository;
    this.molecularProfileService = molecularProfileService;
  }

  /**
   * @param sampleListId sample list restricting the samples, or null when {@code sampleIds} is used
   * @param sampleIds stable sample ids restricting the samples, or null when {@code sampleListId}
   *     is used
   * @throws MolecularProfileNotFoundException if the profile does not exist or is not a mutation
   *     profile
   */
  public List<MutationSpectrum> execute(
      String molecularProfileId, String sampleListId, List<String> sampleIds)
      throws MolecularProfileNotFoundException {

    MolecularProfile profile = molecularProfileService.getMolecularProfile(molecularProfileId);
    MolecularAlterationType type = profile.getMolecularAlterationType();
    if (type != MolecularAlterationType.MUTATION_EXTENDED
        && type != MolecularAlterationType.MUTATION_UNCALLED) {
      throw new MolecularProfileNotFoundException(molecularProfileId);
    }
    if (sampleIds != null && sampleIds.isEmpty()) {
      return Collections.emptyList();
    }
    return mutationRepository.getMutationSpectrums(molecularProfileId, sampleListId, sampleIds);
  }
}
