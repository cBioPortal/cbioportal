package org.cbioportal.domain.geneset_data.usecase;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.geneset_data.repository.GenesetDataRepository;
import org.cbioportal.legacy.model.GenesetMolecularData;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.springframework.stereotype.Service;

/** Fetches gene set scores (e.g. GSVA scores) of a molecular profile. */
@Service
public class FetchGenesetDataUseCase {

  private final GenesetDataRepository genesetDataRepository;
  private final MolecularProfileService molecularProfileService;
  private final SampleListRepository sampleListRepository;

  public FetchGenesetDataUseCase(
      GenesetDataRepository genesetDataRepository,
      MolecularProfileService molecularProfileService,
      SampleListRepository sampleListRepository) {
    this.genesetDataRepository = genesetDataRepository;
    this.molecularProfileService = molecularProfileService;
    this.sampleListRepository = sampleListRepository;
  }

  /**
   * @param sampleListId sample list whose sample ids select the samples of the profile's study, or
   *     null
   * @param sampleIds explicit stable sample ids, or null for all samples of the profile when no
   *     sample list is given
   * @throws SampleListNotFoundException if a sample list is given that does not exist
   */
  public List<GenesetMolecularData> execute(
      String molecularProfileId,
      String sampleListId,
      List<String> sampleIds,
      List<String> genesetIds)
      throws MolecularProfileNotFoundException, SampleListNotFoundException {

    // throws if the profile does not exist
    molecularProfileService.getMolecularProfile(molecularProfileId);

    List<String> requestedSampleIds = sampleIds;
    if (sampleListId != null) {
      if (sampleListRepository.getSampleList(sampleListId) == null) {
        throw new SampleListNotFoundException(sampleListId);
      }
      requestedSampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
    }
    if (requestedSampleIds != null && requestedSampleIds.isEmpty()) {
      return Collections.emptyList();
    }
    return genesetDataRepository.getGenesetData(molecularProfileId, requestedSampleIds, genesetIds);
  }
}
