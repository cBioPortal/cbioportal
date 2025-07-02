package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

/**
 * This class is a silenced version of the VirtualStudyService. It overrides the
 * getPublishedVirtualStudies methods to return an empty list. This is used to silence the published
 * virtual studies in the application.
 */
public class SilencedPublishedVSService implements VirtualStudyService {
  private final VirtualStudyService virtualStudyService;

  public SilencedPublishedVSService(VirtualStudyService virtualStudyService) {
    this.virtualStudyService = virtualStudyService;
  }

  /**
   * Silenced method to return published virtual studies.
   *
   * @return empty list
   */
  @Override
  public List<VirtualStudy> getPublishedVirtualStudies() {
    return Collections.emptyList();
  }

  /**
   * Silenced method to return published virtual studies with a keyword.
   *
   * @param keyword
   * @return empty list
   */
  @Override
  public List<VirtualStudy> getPublishedVirtualStudies(String keyword) {
    return Collections.emptyList();
  }

  @Override
  public String calculateVirtualPatientId(
      String materializedStudyId, String materializedPatientId) {
    return virtualStudyService.calculateVirtualPatientId(
        materializedStudyId, materializedPatientId);
  }

  @Override
  public String calculateVirtualSampleId(String materializedStudyId, String materializedSampleId) {
    return virtualStudyService.calculateVirtualSampleId(materializedStudyId, materializedSampleId);
  }

  @Override
  public ClinicalData virtualizeClinicalData(String virtualStudyId, ClinicalData clinicalData) {
    return virtualStudyService.virtualizeClinicalData(virtualStudyId, clinicalData);
  }

  @Override
  public Set<String> getPublishedVirtualStudyIds() {
    return virtualStudyService.getPublishedVirtualStudyIds();
  }

  @Override
  public Map<StudyScopedId, StudyScopedId> getVirtualToMaterializedStudySamplePairs() {
    return virtualStudyService.getVirtualToMaterializedStudySamplePairs();
  }

  @Override
  public Map<StudyScopedId, StudyScopedId> getVirtualToMaterializedStudyPatientPairs() {
    return virtualStudyService.getVirtualToMaterializedStudyPatientPairs();
  }

  @Override
  public Map<StudyScopedId, Set<String>> toMaterializedStudySamplePairsMap(
      List<StudyScopedId> studyScopedIds) {
    return virtualStudyService.toMaterializedStudySamplePairsMap(studyScopedIds);
  }

  @Override
  public Map<StudyScopedId, Set<String>> toMaterializedStudyPatientPairsMap(
      List<StudyScopedId> studyScopedIds) {
    return virtualStudyService.toMaterializedStudyPatientPairsMap(studyScopedIds);
  }

  @Override
  public Pair<List<StudyScopedId>, List<StudyScopedId>> splitMaterialisedAndVirtualStudySamplePairs(
      List<StudyScopedId> studyScopedIds) {
    return virtualStudyService.splitMaterialisedAndVirtualStudySamplePairs(studyScopedIds);
  }

  @Override
  public List<StudyScopedId> toStudySamplePairs(List<String> studyIds, List<String> sampleIds) {
    return virtualStudyService.toStudySamplePairs(studyIds, sampleIds);
  }

  @Override
  public Pair<List<String>, List<String>> toStudyAndSampleIdLists(
      Iterable<StudyScopedId> studySamplePairs) {
    return virtualStudyService.toStudyAndSampleIdLists(studySamplePairs);
  }

  @Override
  public Sample virtualizeSample(String virtualStudyId, Sample sample) {
    return virtualStudyService.virtualizeSample(virtualStudyId, sample);
  }

  @Override
  public Map<String, Pair<String, String>> toMolecularProfileInfo(Set<String> molecularProfileIds) {
    return virtualStudyService.toMolecularProfileInfo(molecularProfileIds);
  }

  @Override
  public DiscreteCopyNumberData virtualizeDiscreteCopyNumber(
      String vitualStudyId, DiscreteCopyNumberData dcn) {
    return virtualStudyService.virtualizeDiscreteCopyNumber(vitualStudyId, dcn);
  }

  @Override
  public Mutation virtualizeMutation(String virtualStudyId, Mutation m) {
    return virtualStudyService.virtualizeMutation(virtualStudyId, m);
  }

  @Override
  public StructuralVariant virtualizeStructuralVariant(
      String virtualStudyId, StructuralVariant sv) {
    return virtualStudyService.virtualizeStructuralVariant(virtualStudyId, sv);
  }

  @Override
  public CopyNumberSeg virtualizeCopyNumberSeg(String virtualStudyId, CopyNumberSeg segment) {
    return virtualStudyService.virtualizeCopyNumberSeg(virtualStudyId, segment);
  }

  @Override
  public VirtualStudy getVirtualStudy(String id) {
    return virtualStudyService.getVirtualStudy(id);
  }

  @Override
  public Optional<VirtualStudy> getVirtualStudyByIdIfExists(String id) {
    return virtualStudyService.getVirtualStudyByIdIfExists(id);
  }

  @Override
  public List<VirtualStudy> getUserVirtualStudies(String user) {
    return virtualStudyService.getUserVirtualStudies(user);
  }

  @Override
  public void publishVirtualStudy(String id, String typeOfCancerId, String pmid) {
    virtualStudyService.publishVirtualStudy(id, typeOfCancerId, pmid);
  }

  @Override
  public void unPublishVirtualStudy(String id) {
    virtualStudyService.unPublishVirtualStudy(id);
  }

  @Override
  public CancerStudy toCancerStudy(VirtualStudy vs) {
    return virtualStudyService.toCancerStudy(vs);
  }
}
