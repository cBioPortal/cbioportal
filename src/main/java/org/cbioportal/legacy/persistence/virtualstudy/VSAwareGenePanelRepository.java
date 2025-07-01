package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Set;
import org.cbioportal.legacy.model.GenePanel;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.GenePanelRepository;
import org.cbioportal.legacy.service.VirtualStudyService;

// TODO improve how this service works! It seems to have multiple bugs and missing features.
public class VSAwareGenePanelRepository implements GenePanelRepository {

  private final VirtualStudyService virtualStudyService;
  private final GenePanelRepository genePanelRepository;
  private final VSAwareMolecularProfileRepository molecularProfileRepository;

  public VSAwareGenePanelRepository(
      VirtualStudyService virtualStudyService,
      GenePanelRepository genePanelRepository,
      VSAwareMolecularProfileRepository molecularProfileRepository) {
    this.virtualStudyService = virtualStudyService;
    this.genePanelRepository = genePanelRepository;
    this.molecularProfileRepository = molecularProfileRepository;
  }

  @Override
  public List<GenePanel> getAllGenePanels(
      String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
    // TODO missing virtual gene panels
    return genePanelRepository.getAllGenePanels(
        projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public BaseMeta getMetaGenePanels() {
    return genePanelRepository.getMetaGenePanels();
  }

  @Override
  public GenePanel getGenePanel(String genePanelId) {
    return genePanelRepository.getGenePanel(genePanelId);
  }

  @Override
  public List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection) {
    return genePanelRepository.fetchGenePanels(genePanelIds, projection);
  }

  @Override
  public List<GenePanelData> getGenePanelDataBySampleListId(
      String molecularProfileId, String sampleListId) {
    // TODO we need vs aware sample list repository to implement this method
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds) {
    return fetchGenePanelDataByMolecularProfileId(molecularProfileId).stream()
        .filter(gp -> sampleIds.contains(gp.getSampleId()))
        .toList();
  }

  @Override
  public List<GenePanelData> fetchGenePanelDataByMolecularProfileIds(
      Set<String> molecularProfileIds) {
    return molecularProfileIds.stream()
        .flatMap(profileId -> this.fetchGenePanelDataByMolecularProfileId(profileId).stream())
        .toList();
  }

  @Override
  public List<GenePanelData> fetchGenePanelDataByMolecularProfileId(String molecularProfileId) {
    MolecularProfile molecularProfile =
        molecularProfileRepository.getMolecularProfile(molecularProfileId);
    if (molecularProfile == null) {
      return List.of();
    }
    if (virtualStudyService
        .getVirtualStudyByIdIfExists(molecularProfile.getCancerStudyIdentifier())
        .isPresent()) {
      // TODO fine better way to get the original stable ID
      String originalMolecularProfileId =
          molecularProfile
              .getStableId()
              .replace(molecularProfile.getCancerStudyIdentifier() + "_", "");
      return genePanelRepository
          // TODO how about filtering by sample ids that are in the virtual study?
          .fetchGenePanelDataByMolecularProfileId(originalMolecularProfileId)
          .stream()
          .map(gp -> virtualizeGenePanel(molecularProfile, gp))
          .toList();
    } else {
      return genePanelRepository.fetchGenePanelDataByMolecularProfileId(molecularProfileId);
    }
  }

  private GenePanelData virtualizeGenePanel(MolecularProfile vsMolecularProfile, GenePanelData gp) {
    GenePanelData virtualGenePanelData = new GenePanelData();
    virtualGenePanelData.setMolecularProfileId(vsMolecularProfile.getStableId());
    // TODO reuse ids calculation
    virtualGenePanelData.setSampleId(gp.getStudyId() + "_" + gp.getSampleId());
    virtualGenePanelData.setPatientId(gp.getStudyId() + "_" + gp.getPatientId());

    virtualGenePanelData.setStudyId(virtualGenePanelData.getStudyId());
    virtualGenePanelData.setGenePanelId(gp.getGenePanelId());
    virtualGenePanelData.setProfiled(gp.getProfiled());
    return virtualGenePanelData;
  }

  @Override
  public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(
      List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {
    // TODO we need vs aware sample list repository to implement this method
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfilesByPatientIds(
      List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers) {
    // TODO we need vs aware sample list repository to implement this method
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds) {
    return genePanelRepository.getGenesOfPanels(genePanelIds);
  }
}
