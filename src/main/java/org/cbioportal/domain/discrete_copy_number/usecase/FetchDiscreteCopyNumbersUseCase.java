package org.cbioportal.domain.discrete_copy_number.usecase;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.cbioportal.domain.discrete_copy_number.repository.DiscreteCopyNumberRepository;
import org.cbioportal.domain.genomic_data.usecase.FetchGeneMolecularDataUseCase;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Fetches discrete copy number alterations of a profile.
 *
 * <p>Homozygous deletions and amplifications are stored as CNA events. The other alterations
 * (gains, shallow deletions and diploid) are not, so they are derived from the profile's gene-level
 * values.
 */
@Service
public class FetchDiscreteCopyNumbersUseCase {

  private final DiscreteCopyNumberRepository discreteCopyNumberRepository;
  private final MolecularProfileService molecularProfileService;
  private final FetchGeneMolecularDataUseCase fetchGeneMolecularDataUseCase;

  public FetchDiscreteCopyNumbersUseCase(
      DiscreteCopyNumberRepository discreteCopyNumberRepository,
      MolecularProfileService molecularProfileService,
      FetchGeneMolecularDataUseCase fetchGeneMolecularDataUseCase) {
    this.discreteCopyNumberRepository = discreteCopyNumberRepository;
    this.molecularProfileService = molecularProfileService;
    this.fetchGeneMolecularDataUseCase = fetchGeneMolecularDataUseCase;
  }

  /**
   * @param sampleListId sample list restricting the samples, or null when {@code sampleIds} is used
   * @param sampleIds stable sample ids restricting the samples, or null when {@code sampleListId}
   *     is used
   * @throws MolecularProfileNotFoundException if the profile does not exist or is not a discrete
   *     copy number profile
   */
  public List<DiscreteCopyNumberData> execute(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      boolean includeGene)
      throws MolecularProfileNotFoundException {

    validateMolecularProfile(molecularProfileId);
    if (isHomdelOrAmpOnly(alterationTypes)) {
      return discreteCopyNumberRepository.getDiscreteCopyNumbers(
          molecularProfileId, sampleListId, sampleIds, entrezGeneIds, alterationTypes, includeGene);
    }
    return deriveFromMolecularData(
        molecularProfileId, sampleListId, sampleIds, entrezGeneIds, alterationTypes, includeGene);
  }

  public int count(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes)
      throws MolecularProfileNotFoundException {

    validateMolecularProfile(molecularProfileId);
    if (isHomdelOrAmpOnly(alterationTypes)) {
      return discreteCopyNumberRepository.countDiscreteCopyNumbers(
          molecularProfileId, sampleListId, sampleIds, entrezGeneIds, alterationTypes);
    }
    return deriveFromMolecularData(
            molecularProfileId, sampleListId, sampleIds, entrezGeneIds, alterationTypes, false)
        .size();
  }

  private List<DiscreteCopyNumberData> deriveFromMolecularData(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<Integer> alterationTypes,
      boolean includeGene)
      throws MolecularProfileNotFoundException {

    List<GeneMolecularData> molecularData =
        sampleListId != null
            ? fetchGeneMolecularDataUseCase.executeBySampleList(
                molecularProfileId, sampleListId, entrezGeneIds, includeGene)
            : fetchGeneMolecularDataUseCase.executeBySampleIds(
                molecularProfileId, sampleIds, entrezGeneIds, includeGene);

    return molecularData.stream()
        .map(data -> toDiscreteCopyNumber(data, alterationTypes))
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<DiscreteCopyNumberData> toDiscreteCopyNumber(
      GeneMolecularData data, List<Integer> alterationTypes) {
    Integer alteration = parseAlteration(data.getValue());
    if (alteration == null || !alterationTypes.contains(alteration)) {
      return Optional.empty();
    }
    DiscreteCopyNumberData copyNumber = new DiscreteCopyNumberData();
    copyNumber.setMolecularProfileId(data.getMolecularProfileId());
    copyNumber.setStudyId(data.getStudyId());
    copyNumber.setPatientId(data.getPatientId());
    copyNumber.setSampleId(data.getSampleId());
    copyNumber.setEntrezGeneId(data.getEntrezGeneId());
    copyNumber.setGene(data.getGene());
    copyNumber.setAlteration(alteration);
    return Optional.of(copyNumber);
  }

  private Integer parseAlteration(String value) {
    try {
      return value == null ? null : Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private boolean isHomdelOrAmpOnly(List<Integer> alterationTypes) {
    return !alterationTypes.contains(-1)
        && !alterationTypes.contains(0)
        && !alterationTypes.contains(1);
  }

  private void validateMolecularProfile(String molecularProfileId)
      throws MolecularProfileNotFoundException {
    MolecularProfile profile = molecularProfileService.getMolecularProfile(molecularProfileId);
    if (profile.getMolecularAlterationType() != MolecularAlterationType.COPY_NUMBER_ALTERATION
        || !Objects.equals(MolecularProfile.DataType.DISCRETE.name(), profile.getDatatype())) {
      throw new MolecularProfileNotFoundException(molecularProfileId);
    }
  }
}
