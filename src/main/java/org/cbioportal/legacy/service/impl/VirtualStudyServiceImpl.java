package org.cbioportal.legacy.service.impl;

import static org.cbioportal.legacy.web.parameter.VirtualStudyData.ALL_USERS;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.TypeOfCancer;
import org.cbioportal.legacy.service.CancerTypeService;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.service.exception.CancerTypeNotFoundException;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.util.StudyViewFilterApplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class VirtualStudyServiceImpl implements VirtualStudyService {
  private static final Logger LOG = LoggerFactory.getLogger(VirtualStudyServiceImpl.class);

  private final SessionServiceRequestHandler sessionServiceRequestHandler;
  private final StudyViewFilterApplier studyViewFilterApplier;
  private final CancerTypeService cancerTypeService;

  public VirtualStudyServiceImpl(
      CancerTypeService cancerTypeService,
      SessionServiceRequestHandler sessionServiceRequestHandler,
      @Lazy StudyViewFilterApplier studyViewFilterApplier) {
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

  // We also need to invalidate the cache for published dynamic virtual studies when a source study
  // is updated. See CacheController
  @Cacheable(value = "publishedVirtualStudies")
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
  @CachePut(value = "publishedVirtualStudies", key = "#id")
  @Override
  public void publishVirtualStudy(String id, String typeOfCancerId, String pmid) {
    VirtualStudy virtualStudyDataToPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
    VirtualStudyData virtualStudyData = virtualStudyDataToPublish.getData();
    updateStudyMetadataFieldsIfSpecified(virtualStudyData, typeOfCancerId, pmid);
    virtualStudyData.markAsPublished();
    sessionServiceRequestHandler.updateVirtualStudy(virtualStudyDataToPublish);
  }

  /**
   * Un-publish virtual study
   *
   * @param id - id of published virtual study to un-publish
   */
  @CacheEvict(value = "publishedVirtualStudies", key = "#id")
  @Override
  public void unPublishVirtualStudy(String id) {
    VirtualStudy virtualStudyToUnPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
    if (virtualStudyToUnPublish == null) {
      throw new NoSuchElementException(
          "The virtual study with id=" + id + " has not been found in the published list.");
    }
    VirtualStudyData virtualStudyData = virtualStudyToUnPublish.getData();
    if (!virtualStudyData.isPublished()) {
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
}
