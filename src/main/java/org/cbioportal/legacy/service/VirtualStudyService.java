package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.util.StudyViewFilterApplier;
import org.springframework.stereotype.Service;

@Service
public class VirtualStudyService {
  private final SessionServiceRequestHandler sessionServiceRequestHandler;
  private final StudyViewFilterApplier studyViewFilterApplier;

  public VirtualStudyService(
      SessionServiceRequestHandler sessionServiceRequestHandler,
      StudyViewFilterApplier studyViewFilterApplier) {
    this.sessionServiceRequestHandler = sessionServiceRequestHandler;
    this.studyViewFilterApplier = studyViewFilterApplier;
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
}
