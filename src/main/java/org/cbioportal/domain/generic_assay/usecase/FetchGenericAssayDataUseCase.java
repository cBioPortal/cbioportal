package org.cbioportal.domain.generic_assay.usecase;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.generic_assay.repository.GenericAssayDataRepository;
import org.cbioportal.legacy.model.GenericAssayData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Fetches the values of a generic assay profile for the samples of a sample list or for explicit
 * sample ids. A sample list is resolved to sample ids, which are looked up in the study of the
 * profile (as the legacy implementation did). Data is always restricted to that study.
 */
@Service
public class FetchGenericAssayDataUseCase {

  private final GenericAssayDataRepository genericAssayDataRepository;
  private final MolecularProfileService molecularProfileService;
  private final SampleListRepository sampleListRepository;

  public FetchGenericAssayDataUseCase(
      GenericAssayDataRepository genericAssayDataRepository,
      MolecularProfileService molecularProfileService,
      SampleListRepository sampleListRepository) {
    this.genericAssayDataRepository = genericAssayDataRepository;
    this.molecularProfileService = molecularProfileService;
    this.sampleListRepository = sampleListRepository;
  }

  /**
   * @param sampleListId sample list restricting the samples, or null when {@code sampleIds} is used
   * @param sampleIds stable sample ids restricting the samples, or null when {@code sampleListId}
   *     is used
   * @throws MolecularProfileNotFoundException if the profile does not exist or is not a generic
   *     assay profile
   */
  public List<GenericAssayData> execute(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<String> genericAssayStableIds)
      throws MolecularProfileNotFoundException {

    MolecularProfile molecularProfile =
        molecularProfileService.getMolecularProfile(molecularProfileId);
    if (molecularProfile.getMolecularAlterationType() != MolecularAlterationType.GENERIC_ASSAY) {
      throw new MolecularProfileNotFoundException(molecularProfileId);
    }
    List<String> requestedSampleIds =
        sampleListId != null
            ? sampleListRepository.getAllSampleIdsInSampleList(sampleListId)
            : sampleIds;
    if (requestedSampleIds != null && requestedSampleIds.isEmpty()) {
      return Collections.emptyList();
    }

    String studyId = molecularProfile.getCancerStudyIdentifier();
    // Profile ids are "<studyId>_<profileType>"; the derived table is keyed by the latter.
    String profileType = molecularProfileId.substring(studyId.length() + 1);
    return genericAssayDataRepository.getGenericAssayData(
        studyId, molecularProfileId, profileType, requestedSampleIds, genericAssayStableIds);
  }
}
