package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.legacy.model.ClinicalEventKeyCode;
import org.cbioportal.legacy.model.ClinicalEventSample;
import org.cbioportal.legacy.model.Treatment;
import org.cbioportal.legacy.persistence.TreatmentRepository;

public class VSAwareTreatmentRepository implements TreatmentRepository {

  private final VirtualizationService virtualizationService;
  private final TreatmentRepository treatmentRepository;

  public VSAwareTreatmentRepository(
      VirtualizationService virtualizationService, TreatmentRepository treatmentRepository) {
    this.virtualizationService = virtualizationService;
    this.treatmentRepository = treatmentRepository;
  }

  @Override
  public Map<String, List<Treatment>> getTreatmentsByPatientId(
      List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key) {
    return getTreatments(sampleIds, studyIds, key).stream()
        .collect(java.util.stream.Collectors.groupingBy(Treatment::getPatientId));
  }

  @Override
  public List<Treatment> getTreatments(
      List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key) {
    return virtualizationService.handleStudySampleData(
        studyIds,
        sampleIds,
        Treatment::getStudyId,
        Treatment::getSampleId,
        (stids, sids) -> treatmentRepository.getTreatments(sids, stids, key),
        this::virtualizeTreatment);
  }

  @Override
  public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(
      List<String> sampleIds, List<String> studyIds) {
    List<ClinicalEventSample> clinicalEventSamples =
        virtualizationService.handleStudySampleData(
            studyIds,
            sampleIds,
            ClinicalEventSample::getStudyId,
            ClinicalEventSample::getSampleId,
            (stids, sids) ->
                treatmentRepository.getSamplesByPatientId(sids, stids).values().stream()
                    .flatMap(Collection::stream)
                    .toList(),
            this::virtualizeClinicalEventSample);
    return clinicalEventSamples.stream()
        .collect(java.util.stream.Collectors.groupingBy(ClinicalEventSample::getPatientId));
  }

  @Override
  public Map<String, List<ClinicalEventSample>> getShallowSamplesByPatientId(
      List<String> sampleIds, List<String> studyIds) {
    List<ClinicalEventSample> clinicalEventSamples =
        virtualizationService.handleStudySampleData(
            studyIds,
            sampleIds,
            ClinicalEventSample::getStudyId,
            ClinicalEventSample::getSampleId,
            (stids, sids) ->
                treatmentRepository.getShallowSamplesByPatientId(sids, stids).values().stream()
                    .flatMap(Collection::stream)
                    .toList(),
            this::virtualizeClinicalEventSample);
    return clinicalEventSamples.stream()
        .collect(java.util.stream.Collectors.groupingBy(ClinicalEventSample::getPatientId));
  }

  @Override
  public Boolean hasTreatmentData(List<String> studies, ClinicalEventKeyCode key) {
    Map<String, Set<String>> materializedStudyIds =
        virtualizationService.toMaterializedStudyIds((studies));
    return treatmentRepository.hasTreatmentData(
        materializedStudyIds.keySet().stream().toList(), key);
  }

  @Override
  public Boolean hasSampleTimelineData(List<String> studies) {
    Map<String, Set<String>> materializedStudyIds =
        virtualizationService.toMaterializedStudyIds((studies));
    return treatmentRepository.hasSampleTimelineData(
        materializedStudyIds.keySet().stream().toList());
  }

  private Treatment virtualizeTreatment(String virtualStudyId, Treatment treatment) {
    Treatment virtualizedTreatment = new Treatment();
    virtualizedTreatment.setTreatment(treatment.getTreatment());
    virtualizedTreatment.setStudyId(virtualStudyId);
    virtualizedTreatment.setPatientId(treatment.getPatientId());
    virtualizedTreatment.setSampleId(treatment.getSampleId());
    virtualizedTreatment.setStart(treatment.getStart());
    virtualizedTreatment.setStop(treatment.getStop());
    return virtualizedTreatment;
  }

  private ClinicalEventSample virtualizeClinicalEventSample(
      String virtualStudyId, ClinicalEventSample sample) {
    ClinicalEventSample virtualizedSample = new ClinicalEventSample();
    virtualizedSample.setPatientId(sample.getPatientId());
    virtualizedSample.setSampleId(sample.getSampleId());
    virtualizedSample.setStudyId(virtualStudyId);
    virtualizedSample.setTimeTaken(sample.getTimeTaken());
    return virtualizedSample;
  }
}
