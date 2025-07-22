package org.cbioportal.legacy.persistence.virtualstudy;

import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.calculateVirtualMolecularProfileId;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.MolecularProfileRepository;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.parameter.sort.MolecularProfileSortBy;

public class VSAwareMolecularProfileRepository implements MolecularProfileRepository {

  private final VirtualizationService virtualizationService;
  private final MolecularProfileRepository molecularProfileRepository;

  public VSAwareMolecularProfileRepository(
      VirtualizationService virtualizationService,
      MolecularProfileRepository molecularProfileRepository) {
    this.virtualizationService = virtualizationService;
    this.molecularProfileRepository = molecularProfileRepository;
  }

  @Override
  public List<MolecularProfile> getAllMolecularProfiles(
      String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
    List<MolecularProfile> molecularProfiles =
        molecularProfileRepository.getAllMolecularProfiles(
            projection, pageSize, pageNumber, sortBy, direction);
    List<MolecularProfile> virtualMolecularProfiles =
        getAllVirtualMolecularProfiles(molecularProfiles);

    Stream<MolecularProfile> resultStream =
        Stream.concat(molecularProfiles.stream(), virtualMolecularProfiles.stream());

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
  }

  private Comparator<MolecularProfile> composeComparator(String sortBy, String direction) {
    MolecularProfileSortBy mp = MolecularProfileSortBy.valueOf(sortBy);
    Comparator<MolecularProfile> result =
        switch (mp) {
          case molecularProfileId -> Comparator.comparing(MolecularProfile::getStableId);
          case molecularAlterationType ->
              Comparator.comparing(MolecularProfile::getMolecularAlterationType);
          case datatype -> Comparator.comparing(MolecularProfile::getDatatype);
          case name -> Comparator.comparing(MolecularProfile::getName);
          case description -> Comparator.comparing(MolecularProfile::getDescription);
          case showProfileInAnalysisTab ->
              Comparator.comparing(MolecularProfile::getShowProfileInAnalysisTab);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  public List<MolecularProfile> getAllVirtualMolecularProfiles(
      List<MolecularProfile> molecularProfiles) {
    Map<String, List<MolecularProfile>> molecularProfilesByStudyId =
        molecularProfiles.stream()
            .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));
    return virtualizationService.getPublishedVirtualStudies().stream()
        .flatMap(
            virtualStudy -> {
              List<String> studyIds =
                  virtualStudy.getData().getStudies().stream()
                      .map(VirtualStudySamples::getId)
                      .toList();
              // TODO can we check if any of samples in the virtual study is in the molecular
              // profile?
              return studyIds.stream()
                  .flatMap(
                      studyId ->
                          molecularProfilesByStudyId.get(studyId).stream()
                              .map(
                                  molecularProfile -> {
                                    return virtualizeMolecularProfile(
                                        molecularProfile, virtualStudy.getId());
                                    // TODO throw an exception if there are more then one molecular
                                    // profile
                                  })
                              .collect(Collectors.groupingBy(MolecularProfile::getStableId))
                              .values()
                              .stream()
                              .map(List::getFirst));
            })
        .toList();
  }

  @Override
  public BaseMeta getMetaMolecularProfiles() {
    BaseMeta meta = new BaseMeta();
    meta.setTotalCount(
        getAllMolecularProfiles(Projection.ID.name(), null, null, null, null).size());
    return meta;
  }

  @Override
  public MolecularProfile getMolecularProfile(String molecularProfileId) {
    return getAllMolecularProfiles(Projection.DETAILED.name(), null, null, null, null).stream()
        .filter(molecularProfile -> molecularProfile.getStableId().equals(molecularProfileId))
        .findFirst()
        .orElse(null);
  }

  private MolecularProfile virtualizeMolecularProfile(
      MolecularProfile molecularProfile, String virtualStudyId) {
    MolecularProfile virtualMolecularProfile = new MolecularProfile();

    /**
     * Otherwise, we get the following error: java.lang.NullPointerException: Cannot invoke
     * "java.lang.Integer.toString()" because the return value of
     * "org.cbioportal.legacy.model.MolecularProfile.getMolecularProfileId()" is null at
     * org.cbioportal.legacy.persistence.mybatis.AlterationMyBatisRepository.lambda$getSampleAlterationGeneCounts$0(AlterationMyBatisRepository.java:51)
     * ~[classes/:na]
     */
    // virtualMolecularProfile.setMolecularProfileId(molecularProfile.getMolecularProfileId());

    virtualMolecularProfile.setMolecularAlterationType(
        molecularProfile.getMolecularAlterationType());
    virtualMolecularProfile.setCancerStudyIdentifier(virtualStudyId);
    virtualMolecularProfile.setDatatype(molecularProfile.getDatatype());
    virtualMolecularProfile.setStableId(
        calculateVirtualMolecularProfileId(
            molecularProfile.getStableId(),
            virtualStudyId,
            molecularProfile.getCancerStudyIdentifier()));
    virtualMolecularProfile.setName(molecularProfile.getName());
    virtualMolecularProfile.setDescription(molecularProfile.getDescription());
    virtualMolecularProfile.setCancerStudy(molecularProfile.getCancerStudy());
    virtualMolecularProfile.setPatientLevel(molecularProfile.getPatientLevel());
    virtualMolecularProfile.setGenericAssayType(molecularProfile.getGenericAssayType());
    virtualMolecularProfile.setPivotThreshold(molecularProfile.getPivotThreshold());
    virtualMolecularProfile.setShowProfileInAnalysisTab(
        molecularProfile.getShowProfileInAnalysisTab());
    virtualMolecularProfile.setSortOrder(molecularProfile.getSortOrder());
    return virtualMolecularProfile;
  }

  @Override
  public List<MolecularProfile> getMolecularProfiles(
      Set<String> molecularProfileIds, String projection) {
    return getAllMolecularProfiles(projection, null, null, null, null).stream()
        .filter(molecularProfile -> molecularProfileIds.contains(molecularProfile.getStableId()))
        .collect(Collectors.toList());
  }

  @Override
  public BaseMeta getMetaMolecularProfiles(Set<String> molecularProfileIds) {
    BaseMeta meta = new BaseMeta();
    meta.setTotalCount(getMolecularProfiles(molecularProfileIds, Projection.ID.name()).size());
    return meta;
  }

  @Override
  public List<MolecularProfile> getAllMolecularProfilesInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return getAllMolecularProfiles(projection, pageSize, pageNumber, sortBy, direction).stream()
        .filter(molecularProfile -> molecularProfile.getCancerStudyIdentifier().equals(studyId))
        .collect(Collectors.toList());
  }

  @Override
  public BaseMeta getMetaMolecularProfilesInStudy(String studyId) {
    BaseMeta meta = new BaseMeta();
    meta.setTotalCount(
        getAllMolecularProfilesInStudy(studyId, Projection.ID.name(), null, null, null, null)
            .size());
    return meta;
  }

  @Override
  public List<MolecularProfile> getMolecularProfilesInStudies(
      List<String> studyIds, String projection) {
    return getAllMolecularProfiles(projection, null, null, null, null).stream()
        .filter(molecularProfile -> studyIds.contains(molecularProfile.getCancerStudyIdentifier()))
        .collect(Collectors.toList());
  }

  @Override
  public BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds) {
    BaseMeta meta = new BaseMeta();
    meta.setTotalCount(getMolecularProfilesInStudies(studyIds, Projection.ID.name()).size());
    return meta;
  }

  @Override
  public List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId) {
    // we don't support this for virtual studies
    return molecularProfileRepository.getMolecularProfilesReferredBy(referringMolecularProfileId);
  }

  @Override
  public List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId) {
    // we don't support this for virtual studies
    return molecularProfileRepository.getMolecularProfilesReferringTo(referredMolecularProfileId);
  }
}
