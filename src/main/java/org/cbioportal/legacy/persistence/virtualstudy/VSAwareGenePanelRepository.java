package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cbioportal.legacy.model.GenePanel;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.GenePanelRepository;
import org.cbioportal.legacy.service.VirtualStudyService;

public class VSAwareGenePanelRepository implements GenePanelRepository {

  private final VirtualStudyService virtualStudyService;
  private final GenePanelRepository genePanelRepository;
  private final VSAwareMolecularProfileRepository molecularProfileRepository;
  private final VSAwareSampleListRepository sampleListRepository;

  public VSAwareGenePanelRepository(
      VirtualStudyService virtualStudyService,
      GenePanelRepository genePanelRepository,
      VSAwareMolecularProfileRepository molecularProfileRepository,
      VSAwareSampleListRepository sampleListRepository) {
    this.virtualStudyService = virtualStudyService;
    this.genePanelRepository = genePanelRepository;
    this.molecularProfileRepository = molecularProfileRepository;
    this.sampleListRepository = sampleListRepository;
  }

  @Override
  public List<GenePanel> getAllGenePanels(
      String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
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
    List<String> sampleIdsList = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
    if (sampleIdsList == null || sampleIdsList.isEmpty()) {
      return Collections.emptyList();
    }
    Set<String> sampleIdsSet = new HashSet<>(sampleIdsList);
    return fetchGenePanelDataByMolecularProfileId(molecularProfileId).stream()
        .filter(gp -> sampleIdsSet.contains(gp.getSampleId()))
        .toList();
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
    virtualGenePanelData.setSampleId(gp.getSampleId());
    virtualGenePanelData.setPatientId(gp.getPatientId());

    virtualGenePanelData.setStudyId(virtualGenePanelData.getStudyId());
    virtualGenePanelData.setGenePanelId(gp.getGenePanelId());
    virtualGenePanelData.setProfiled(gp.getProfiled());
    return virtualGenePanelData;
  }

  @Override
  public List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds) {
    return genePanelRepository.getGenesOfPanels(genePanelIds);
  }
}
