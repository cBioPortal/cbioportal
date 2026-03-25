package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.service.exception.CancerTypeNotFoundException;
import org.cbioportal.legacy.service.exception.DuplicateVirtualStudyException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.util.StudyViewFilterApplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VirtualStudyService {
  private static final Logger LOG = LoggerFactory.getLogger(VirtualStudyService.class);

  public static final String ALL_USERS = "*";
  private final SessionServiceRequestHandler sessionServiceRequestHandler;
  private final StudyViewFilterApplier studyViewFilterApplier;
  private final CancerTypeService cancerTypeService;
  private final StudyService studyService;

  public VirtualStudyService(
      SessionServiceRequestHandler sessionServiceRequestHandler,
      StudyViewFilterApplier studyViewFilterApplier,
      CancerTypeService cancerTypeService,
      StudyService studyService) {
    this.sessionServiceRequestHandler = sessionServiceRequestHandler;
    this.studyViewFilterApplier = studyViewFilterApplier;
    this.cancerTypeService = cancerTypeService;
    this.studyService = studyService;
  }

  public VirtualStudy getVirtualStudy(String id) {
    VirtualStudy virtualStudy = sessionServiceRequestHandler.getVirtualStudyById(id);
    VirtualStudyData virtualStudyData = virtualStudy.getData();
    if (Boolean.TRUE.equals(virtualStudyData.getDynamic())) {
      populateVirtualStudySamples(virtualStudyData);
    }
    return virtualStudy;
  }

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

  public List<VirtualStudy> getPublicVirtualStudies() {
    return getUserVirtualStudies(ALL_USERS);
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

  /**
   * Publishes virtual study optionally updating metadata fields
   *
   * @param id - id of public virtual study to publish
   * @param typeOfCancerId - if specified (not null) update type of cancer of published virtual
   *     study
   * @param pmid - if specified (not null) update PubMed ID of published virtual study
   * @param virtualStudyData - if specified (not null) create new virtual study with this data,
   *     otherwise updates virtual study with the given id
   */
  public void publishVirtualStudy(
      String id, String typeOfCancerId, String pmid, VirtualStudyData virtualStudyData) {
    if (virtualStudyData == null) {
      VirtualStudy virtualStudyDataToPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
      VirtualStudyData storedVirtualStudyData = virtualStudyDataToPublish.getData();
      updateStudyMetadataFieldsIfSpecified(storedVirtualStudyData, typeOfCancerId, pmid);
      storedVirtualStudyData.setUsers(Set.of(ALL_USERS));
      sessionServiceRequestHandler.updateVirtualStudy(virtualStudyDataToPublish);
    } else {
      updateStudyMetadataFieldsIfSpecified(virtualStudyData, typeOfCancerId, pmid);
      virtualStudyData.setUsers(Set.of(ALL_USERS));
      try {
        studyService.getStudy(id);
        throw new DuplicateVirtualStudyException(
            "The study with id="
                + id
                + " already exists. Use a different id for the virtual study.");
      } catch (StudyNotFoundException e) {
        LOG.debug(
            "The study with id={} does not exist, proceeding to create a new virtual study.", id);
      }
      sessionServiceRequestHandler.createVirtualStudy(id, virtualStudyData);
    }
  }

  /**
   * Un-publish virtual study
   *
   * @param id - id of public virtual study to un-publish
   */
  public void unPublishVirtualStudy(String id) {
    VirtualStudy virtualStudyToUnPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
    if (virtualStudyToUnPublish == null) {
      throw new NoSuchElementException(
          "The virtual study with id=" + id + " has not been found in the public list.");
    }
    VirtualStudyData virtualStudyData = virtualStudyToUnPublish.getData();
    checkIfVSWasPublished(id, virtualStudyData);
    virtualStudyData.setUsers(Set.of(virtualStudyData.getOwner()));
    sessionServiceRequestHandler.updateVirtualStudy(virtualStudyToUnPublish);
  }

  /**
   * Drops public virtual study, removing it from the public list
   *
   * @param id - id of public virtual study to drop
   */
  public void dropPublicVirtualStudyById(String id) {
    VirtualStudy virtualStudyToUnPublish = sessionServiceRequestHandler.getVirtualStudyById(id);
    checkIfVSWasPublished(id, virtualStudyToUnPublish.getData());
    sessionServiceRequestHandler.dropVirtualStudy(id);
  }

  private static void checkIfVSWasPublished(String id, VirtualStudyData virtualStudyData) {
    Set<String> users = virtualStudyData.getUsers();
    if (users == null || users.isEmpty() || !users.contains(ALL_USERS)) {
      throw new NoSuchElementException(
          "The virtual study with id=" + id + " has not been found in the public list.");
    }
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
}
