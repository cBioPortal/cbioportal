package org.cbioportal.application.file.services;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.repositories.ClinicalAttributeDataRepository;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributeValue;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;
import org.cbioportal.application.file.utils.CloseableIterator;

/** Service to retrieve clinical data attributes and values for a study */
public class ClinicalAttributeDataService {

  private final ClinicalAttributeDataRepository clinicalAttributeDataRepository;

  public ClinicalAttributeDataService(
      ClinicalAttributeDataRepository clinicalAttributeDataRepository) {
    this.clinicalAttributeDataRepository = clinicalAttributeDataRepository;
  }

  public CloseableIterator<ClinicalAttributeValue> getClinicalSampleAttributeValues(
      String studyId, Set<String> sampleIds) {
    return clinicalAttributeDataRepository.getClinicalSampleAttributeValues(studyId, sampleIds);
  }

  public List<ClinicalAttribute> getClinicalSampleAttributes(String studyId) {
    return clinicalAttributeDataRepository.getClinicalSampleAttributes(studyId);
  }

  public CloseableIterator<ClinicalAttributeValue> getClinicalPatientAttributeValues(
      String studyId, Set<String> sampleIds) {
    return clinicalAttributeDataRepository.getClinicalPatientAttributeValues(studyId, sampleIds);
  }

  public List<ClinicalAttribute> getClinicalPatientAttributes(String studyId) {
    return clinicalAttributeDataRepository.getClinicalPatientAttributes(studyId);
  }

  public boolean hasClinicalPatientAttributes(String studyId, Set<String> sampleIds) {
    return clinicalAttributeDataRepository.hasClinicalPatientAttributes(studyId, sampleIds);
  }

  public boolean hasClinicalSampleAttributes(String studyId, Set<String> sampleIds) {
    return clinicalAttributeDataRepository.hasClinicalSampleAttributes(studyId, sampleIds);
  }

  public boolean hasClinicalTimelineData(String studyId, Set<String> sampleIds) {
    return clinicalAttributeDataRepository.hasClinicalTimelineData(studyId, sampleIds);
  }

  public List<String> getDistinctClinicalEventKeys(String studyId, String eventType) {
    return clinicalAttributeDataRepository.getDistinctClinicalEventKeys(studyId, eventType);
  }

  public CloseableIterator<ClinicalEventData> getClinicalEventData(
      String studyId, String eventType, Set<String> sampleIds) {
    return clinicalAttributeDataRepository.getClinicalEventData(studyId, eventType, sampleIds);
  }

  public CloseableIterator<ClinicalEvent> getClinicalEvents(
      String studyId, String eventType, Set<String> sampleIds) {
    return clinicalAttributeDataRepository.getClinicalEvents(studyId, eventType, sampleIds);
  }

  public List<String> getDistinctEventTypes(String studyId) {
    return clinicalAttributeDataRepository.getDistinctEventTypes(studyId);
  }
}
