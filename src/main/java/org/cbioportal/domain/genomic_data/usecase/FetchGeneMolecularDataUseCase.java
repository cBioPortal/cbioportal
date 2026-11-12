package org.cbioportal.domain.genomic_data.usecase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cbioportal.domain.genomic_data.repository.GeneMolecularDataRepository;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Fetches gene-level molecular data of a single molecular profile for a set of samples, given
 * either as a sample list or as explicit sample ids. Data is always restricted to the study of the
 * requested profile.
 */
@Service
public class FetchGeneMolecularDataUseCase {

  private final GeneMolecularDataRepository geneMolecularDataRepository;
  private final MolecularProfileService molecularProfileService;

  public FetchGeneMolecularDataUseCase(
      GeneMolecularDataRepository geneMolecularDataRepository,
      MolecularProfileService molecularProfileService) {
    this.geneMolecularDataRepository = geneMolecularDataRepository;
    this.molecularProfileService = molecularProfileService;
  }

  public List<GeneMolecularData> executeBySampleList(
      String molecularProfileId,
      String sampleListId,
      List<Integer> entrezGeneIds,
      boolean includeGene)
      throws MolecularProfileNotFoundException {
    return execute(molecularProfileId, sampleListId, null, entrezGeneIds, includeGene);
  }

  public List<GeneMolecularData> executeBySampleIds(
      String molecularProfileId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean includeGene)
      throws MolecularProfileNotFoundException {
    return execute(molecularProfileId, null, sampleIds, entrezGeneIds, includeGene);
  }

  /**
   * Fetches gene-level data of several molecular profiles, possibly of different studies.
   *
   * <p>Unlike the single-profile variant, unknown profile ids (and mutation or structural variant
   * profiles, which hold no gene-level data) are skipped instead of failing the request, as in the
   * legacy implementation.
   *
   * @param molecularProfileIds the profiles to read from
   * @param sampleIds null to fetch all samples of every profile. Otherwise a list parallel to
   *     {@code molecularProfileIds}: entry i is a sample of the study of profile i. Each sample is
   *     then looked up in every requested profile of its own study.
   * @param entrezGeneIds genes to fetch; nothing is returned when empty
   */
  public List<GeneMolecularData> executeInMultipleProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean includeGene) {

    if (entrezGeneIds == null || entrezGeneIds.isEmpty() || molecularProfileIds.isEmpty()) {
      return Collections.emptyList();
    }

    List<MolecularProfile> profiles =
        molecularProfileService.getMolecularProfiles(
            new LinkedHashSet<>(molecularProfileIds), "SUMMARY");
    if (profiles.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> requestedSampleIds = null;
    List<String> sampleStudyIds = null;
    if (sampleIds != null) {
      Map<String, String> studyByProfileId =
          profiles.stream()
              .collect(
                  Collectors.toMap(
                      MolecularProfile::getStableId,
                      MolecularProfile::getCancerStudyIdentifier,
                      (a, b) -> a));
      requestedSampleIds = new ArrayList<>();
      sampleStudyIds = new ArrayList<>();
      for (int i = 0; i < sampleIds.size(); i++) {
        String studyId = studyByProfileId.get(molecularProfileIds.get(i));
        if (studyId != null) {
          requestedSampleIds.add(sampleIds.get(i));
          sampleStudyIds.add(studyId);
        }
      }
      if (requestedSampleIds.isEmpty()) {
        return Collections.emptyList();
      }
    }

    return geneMolecularDataRepository.getGeneMolecularDataInMultipleProfiles(
        profiles, entrezGeneIds, requestedSampleIds, sampleStudyIds, includeGene);
  }

  private List<GeneMolecularData> execute(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      boolean includeGene)
      throws MolecularProfileNotFoundException {

    MolecularProfile molecularProfile = validateMolecularProfile(molecularProfileId);
    if (sampleIds != null && sampleIds.isEmpty()) {
      return Collections.emptyList();
    }

    String studyId = molecularProfile.getCancerStudyIdentifier();
    // Profile ids are "<studyId>_<profileType>"; genetic_alteration_derived is keyed by the latter.
    String profileType = molecularProfileId.substring(studyId.length() + 1);

    return geneMolecularDataRepository.getGeneMolecularData(
        studyId,
        molecularProfileId,
        profileType,
        entrezGeneIds,
        sampleListId,
        sampleIds,
        includeGene);
  }

  private MolecularProfile validateMolecularProfile(String molecularProfileId)
      throws MolecularProfileNotFoundException {
    MolecularProfile molecularProfile =
        molecularProfileService.getMolecularProfile(molecularProfileId);
    MolecularAlterationType type = molecularProfile.getMolecularAlterationType();
    if (type == MolecularAlterationType.MUTATION_EXTENDED
        || type == MolecularAlterationType.MUTATION_UNCALLED
        || type == MolecularAlterationType.STRUCTURAL_VARIANT) {
      throw new MolecularProfileNotFoundException(molecularProfileId);
    }
    return molecularProfile;
  }
}
