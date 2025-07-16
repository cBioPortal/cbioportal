package org.cbioportal.legacy.service.impl;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.model.TypeOfCancer;
import org.cbioportal.legacy.service.CancerTypeService;
import org.cbioportal.legacy.service.SampleService;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.service.exception.CancerTypeNotFoundException;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.util.StudyViewFilterApplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class VirtualStudyServiceImpl implements VirtualStudyService {
  private static final Logger LOG = LoggerFactory.getLogger(VirtualStudyServiceImpl.class);

  private final SampleService sampleService;
  private final SessionServiceRequestHandler sessionServiceRequestHandler;
  private final StudyViewFilterApplier studyViewFilterApplier;
  private final CancerTypeService cancerTypeService;

  public VirtualStudyServiceImpl(
      @Lazy SampleService sampleService,
      CancerTypeService cancerTypeService,
      SessionServiceRequestHandler sessionServiceRequestHandler,
      @Lazy StudyViewFilterApplier studyViewFilterApplier) {
    this.sampleService = sampleService;
    this.cancerTypeService = cancerTypeService;
    this.sessionServiceRequestHandler = sessionServiceRequestHandler;
    this.studyViewFilterApplier = studyViewFilterApplier;
  }

  @Override
  public VirtualStudy getVirtualStudy(String id) {
    VirtualStudy virtualStudy = sessionServiceRequestHandler.getVirtualStudyById(id);
    VirtualStudyData virtualStudyData = virtualStudy.getData();
    if (Boolean.TRUE.equals(virtualStudyData.getDynamic())) {
      populateVirtualStudySamples(virtualStudyData);
    }
    return virtualStudy;
  }

  @Override
  public Optional<VirtualStudy> getVirtualStudyByIdIfExists(String id) {
    return sessionServiceRequestHandler
        .getVirtualStudyByIdIfExists(id)
        .map(
            virtualStudy -> {
              VirtualStudyData virtualStudyData = virtualStudy.getData();
              if (Boolean.TRUE.equals(virtualStudyData.getDynamic())) {
                populateVirtualStudySamples(virtualStudyData);
              }
              return virtualStudy;
            });
  }

  @Override
  public List<VirtualStudy> getUserVirtualStudies(String user) {
    List<VirtualStudy> virtualStudies =
        sessionServiceRequestHandler.getVirtualStudiesAccessibleToUser(user);
    for (VirtualStudy virtualStudy : virtualStudies) {
      VirtualStudyData virtualStudyData = virtualStudy.getData();
      if (Boolean.TRUE.equals(virtualStudyData.getDynamic())) {
        populateVirtualStudySamples(virtualStudyData);
      }
    }
    return virtualStudies;
  }

  /**
   * This method populates the `virtualStudyData` object with a new set of sample IDs retrieved as
   * the result of executing a query based on virtual study view filters. It first applies the
   * filters defined within the study view, runs the query to fetch the relevant sample IDs, and
   * then updates the virtualStudyData to reflect these fresh results. This ensures that the virtual
   * study contains the latest sample IDs.
   *
   * @param virtualStudyData
   */
  private void populateVirtualStudySamples(VirtualStudyData virtualStudyData) {
    List<SampleIdentifier> sampleIdentifiers =
        studyViewFilterApplier.apply(virtualStudyData.getStudyViewFilter());
    Set<VirtualStudySamples> virtualStudySamples = extractVirtualStudySamples(sampleIdentifiers);
    virtualStudyData.setStudies(virtualStudySamples);
  }

  /**
   * Transforms list of sample identifiers to set of virtual study samples
   *
   * @param sampleIdentifiers
   */
  private Set<VirtualStudySamples> extractVirtualStudySamples(
      List<SampleIdentifier> sampleIdentifiers) {
    Map<String, Set<String>> sampleIdsByStudyId = groupSampleIdsByStudyId(sampleIdentifiers);
    return sampleIdsByStudyId.entrySet().stream()
        .map(
            entry -> {
              VirtualStudySamples vss = new VirtualStudySamples();
              vss.setId(entry.getKey());
              vss.setSamples(entry.getValue());
              return vss;
            })
        .collect(Collectors.toSet());
  }

  /**
   * Groups sample IDs by their study ID
   *
   * @param sampleIdentifiers
   */
  private Map<String, Set<String>> groupSampleIdsByStudyId(
      List<SampleIdentifier> sampleIdentifiers) {
    return sampleIdentifiers.stream()
        .collect(
            Collectors.groupingBy(
                SampleIdentifier::getStudyId,
                Collectors.mapping(SampleIdentifier::getSampleId, Collectors.toSet())));
  }

  // TODO implement cache
  @Override
  public List<VirtualStudy> getPublishedVirtualStudies() {
    List<VirtualStudy> virtualStudies =
        sessionServiceRequestHandler.getVirtualStudiesAccessibleToUser(ALL_USERS);
    for (VirtualStudy virtualStudy : virtualStudies) {
      VirtualStudyData virtualStudyData = virtualStudy.getData();
      if (Boolean.TRUE.equals(virtualStudyData.getDynamic())) {
        populateVirtualStudySamples(virtualStudyData);
      }
    }
    return virtualStudies;
  }

  /**
   * Publishes virtual study optionally updating metadata fields
   *
   * @param id - id of virtual study to publish
   * @param typeOfCancerId - if specified (not null) update type of cancer of published virtual
   *     study
   * @param pmid - if specified (not null) update PubMed ID of published virtual study
   */
  // TODO add study id to the cache
  @Override
  public void publishVirtualStudy(String id, String typeOfCancerId, String pmid) {
    VirtualStudy virtualStudyDataToPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
    VirtualStudyData virtualStudyData = virtualStudyDataToPublish.getData();
    updateStudyMetadataFieldsIfSpecified(virtualStudyData, typeOfCancerId, pmid);
    virtualStudyData.setUsers(Set.of(ALL_USERS));
    sessionServiceRequestHandler.updateVirtualStudy(virtualStudyDataToPublish);
  }

  /**
   * Un-publish virtual study
   *
   * @param id - id of published virtual study to un-publish
   */
  // TODO evict study id from the cache
  @Override
  public void unPublishVirtualStudy(String id) {
    VirtualStudy virtualStudyToUnPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
    if (virtualStudyToUnPublish == null) {
      throw new NoSuchElementException(
          "The virtual study with id=" + id + " has not been found in the published list.");
    }
    VirtualStudyData virtualStudyData = virtualStudyToUnPublish.getData();
    Set<String> users = virtualStudyData.getUsers();
    if (users == null || users.isEmpty() || !users.contains(ALL_USERS)) {
      throw new NoSuchElementException(
          "The virtual study with id=" + id + " has not been found in the published list.");
    }
    virtualStudyData.setUsers(Set.of(virtualStudyData.getOwner()));
    sessionServiceRequestHandler.updateVirtualStudy(virtualStudyToUnPublish);
  }

  private void updateStudyMetadataFieldsIfSpecified(
      VirtualStudyData virtualStudyData, String typeOfCancerId, String pmid) {
    if (typeOfCancerId != null) {
      try {
        cancerTypeService.getCancerType(typeOfCancerId);
        virtualStudyData.setTypeOfCancerId(typeOfCancerId);
      } catch (CancerTypeNotFoundException e) {
        LOG.error("No cancer type with id={} were found.", typeOfCancerId);
        throw new IllegalArgumentException("The cancer type is not valid: " + typeOfCancerId);
      }
    }
    if (pmid != null) {
      virtualStudyData.setPmid(pmid);
    }
  }

  static TypeOfCancer mixedTypeOfCancer = new TypeOfCancer();

  static {
    mixedTypeOfCancer.setTypeOfCancerId("mixed");
    mixedTypeOfCancer.setName("Mixed");
  }

  @Override
  public List<VirtualStudy> getPublishedVirtualStudies(String keyword) {
    var keywordFilter = virtualStudyKeywordFilter(keyword);
    return getPublishedVirtualStudies().stream().filter(keywordFilter).toList();
  }

  private static Predicate<? super VirtualStudy> virtualStudyKeywordFilter(String keyword) {
    if (keyword == null || keyword.isEmpty()) {
      return virtualStudy -> true;
    }
    var lcKeyword = keyword.toLowerCase();
    return virtualStudy -> {
      VirtualStudyData data = virtualStudy.getData();
      return (data.getName() != null && data.getName().toLowerCase().contains(lcKeyword))
          || (data.getDescription() != null
              && data.getDescription().toLowerCase().contains(lcKeyword));
    };
  }

  /**
   * Returns a set of IDs of published virtual studies.
   *
   * @return a set of IDs of published virtual studies
   */
  // TODO cahce
  // TODO maybe vs study to materialized study mapping would be more useful
  @Override
  public Set<String> getPublishedVirtualStudyIds() {
    return getPublishedVirtualStudies().stream()
        .map(VirtualStudy::getId)
        .collect(Collectors.toSet());
  }

  /**
   * Returns a map of virtual study-sample pairs to materialized study-sample pairs. The keys are
   * StudySamplePair objects representing the virtual study-sample pairs, and the values are
   * StudySamplePair objects representing the corresponding materialized study-sample pairs.
   *
   * @return a map of virtual to materialized study-sample pairs
   */
  // TODO cahce
  @Override
  public Map<StudyScopedId, StudyScopedId> getVirtualToMaterializedStudySamplePairs() {
    return getPublishedVirtualStudies().stream()
        .flatMap(
            vs ->
                vs.getData().getStudies().stream()
                    .flatMap(
                        virtualStudySamples ->
                            virtualStudySamples.getSamples().stream()
                                .map(
                                    s ->
                                        ImmutablePair.of(
                                            // TODO simplify!
                                            new StudyScopedId(vs.getId(), s),
                                            new StudyScopedId(virtualStudySamples.getId(), s)))))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  /** Returns a map of virtual study-patient pairs to materialized study-patient pairs. */
  // TODO cahce
  private Map<StudyScopedId, StudyScopedId> getVirtualToMaterializedStudyPatientPairs() {
    return getPublishedVirtualStudies().stream()
        .flatMap(
            vs -> {
              List<String> studyIds =
                  vs.getData().getStudies().stream()
                      .flatMap(vss -> vss.getSamples().stream().map(s -> vss.getId()))
                      .toList();
              List<String> sampleIds =
                  vs.getData().getStudies().stream()
                      .flatMap(vss -> vss.getSamples().stream())
                      .toList();
              return sampleService.fetchSamples(studyIds, sampleIds, Projection.ID.name()).stream()
                  .map(s -> new StudyScopedId(s.getCancerStudyIdentifier(), s.getPatientStableId()))
                  .distinct()
                  .map(
                      ssi ->
                          ImmutablePair.of(new StudyScopedId(vs.getId(), ssi.getStableId()), ssi));
            })
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  /**
   * Converts a list of virtual StudySamplePair objects into a map where the keys are materialised
   * StudySamplePair objects and values are sets of study IDs (virtual or regular).
   */
  @Override
  public Map<StudyScopedId, Set<String>> toMaterializedStudySamplePairsMap(
      List<StudyScopedId> studyScopedIds) {
    Map<StudyScopedId, StudyScopedId> vsToMzMap = getVirtualToMaterializedStudySamplePairs();
    return studyScopedIds.stream()
        .map(ssp -> ImmutablePair.of(vsToMzMap.getOrDefault(ssp, ssp), ssp.getStudyStableId()))
        .collect(
            Collectors.toMap(
                Pair::getLeft,
                pair -> Set.of(pair.getRight()),
                (existing, replacement) -> {
                  existing.addAll(replacement);
                  return existing;
                }));
  }

  /**
   * Converts a list of virtual StudyScopedId objects into a map where the keys are materialised
   * StudyScopedId objects and values are sets of study IDs (virtual or regular).
   *
   * @param studyScopedIds
   * @return
   */
  @Override
  public Map<StudyScopedId, Set<String>> toMaterializedStudyPatientPairsMap(
      List<StudyScopedId> studyScopedIds) {
    Map<StudyScopedId, StudyScopedId> vsToMzMap = getVirtualToMaterializedStudyPatientPairs();
    return studyScopedIds.stream()
        .map(ssp -> ImmutablePair.of(vsToMzMap.getOrDefault(ssp, ssp), ssp.getStudyStableId()))
        .collect(
            Collectors.toMap(
                Pair::getLeft,
                pair -> Set.of(pair.getRight()),
                (existing, replacement) -> {
                  existing.addAll(replacement);
                  return existing;
                }));
  }

  @Override
  public Map<String, Pair<String, String>> toMolecularProfileInfo(Set<String> molecularProfileIds) {
    Set<String> allVirtualStudyIds = getPublishedVirtualStudyIds();
    return molecularProfileIds.stream()
        .map(
            mpid -> {
              var matchingVsId =
                  allVirtualStudyIds.stream()
                      .filter(vsid -> mpid.startsWith(vsid + "_"))
                      .findFirst();
              return matchingVsId
                  .map(s -> ImmutableTriple.of(mpid, s, mpid.replace(s + "_", "")))
                  .orElse(null);
            })
        .filter(Objects::nonNull)
        .collect(
            Collectors.toMap(Triple::getLeft, t -> ImmutablePair.of(t.getMiddle(), t.getRight())));
  }
}
