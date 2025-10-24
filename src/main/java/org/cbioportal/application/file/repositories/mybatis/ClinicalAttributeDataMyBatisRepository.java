package org.cbioportal.application.file.repositories.mybatis;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.repositories.ClinicalAttributeDataRepository;
import org.cbioportal.application.file.repositories.mybatis.utils.CursorAdapter;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributeValue;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;
import org.cbioportal.application.file.utils.CloseableIterator;

public class ClinicalAttributeDataMyBatisRepository implements ClinicalAttributeDataRepository {
  private final ClinicalAttributeDataMapper mapper;

  public ClinicalAttributeDataMyBatisRepository(ClinicalAttributeDataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<ClinicalAttribute> getClinicalSampleAttributes(String studyId) {
    return mapper.getClinicalSampleAttributes(studyId);
  }

  @Override
  public CloseableIterator<ClinicalAttributeValue> getClinicalSampleAttributeValues(
      String studyId, Set<String> sampleIds) {
    return new CursorAdapter<>(mapper.getClinicalSampleAttributeValues(studyId, sampleIds));
  }

  @Override
  public List<ClinicalAttribute> getClinicalPatientAttributes(String studyId) {
    return mapper.getClinicalPatientAttributes(studyId);
  }

  @Override
  public CloseableIterator<ClinicalAttributeValue> getClinicalPatientAttributeValues(
      String studyId, Set<String> sampleIds) {
    return new CursorAdapter<>(mapper.getClinicalPatientAttributeValues(studyId, sampleIds));
  }

  @Override
  public boolean hasClinicalPatientAttributes(String studyId, Set<String> sampleIds) {
    return mapper.hasClinicalPatientAttributes(studyId, sampleIds);
  }

  @Override
  public boolean hasClinicalSampleAttributes(String studyId, Set<String> sampleIds) {
    return mapper.hasClinicalSampleAttributes(studyId, sampleIds);
  }

  @Override
  public boolean hasClinicalTimelineData(String studyId, Set<String> sampleIds) {
    return mapper.hasClinicalTimelineData(studyId, sampleIds);
  }

  @Override
  public List<String> getDistinctClinicalEventKeys(String studyId, String eventType) {
    return mapper.getDistinctClinicalEventKeys(studyId, eventType);
  }

  @Override
  public CloseableIterator<ClinicalEventData> getClinicalEventData(
      String studyId, String eventType, Set<String> sampleIds) {
    return new CursorAdapter<>(mapper.getClinicalEventData(studyId, eventType, sampleIds));
  }

  @Override
  public CloseableIterator<ClinicalEvent> getClinicalEvents(
      String studyId, String eventType, Set<String> sampleIds) {
    return new CursorAdapter<>(mapper.getClinicalEvents(studyId, eventType, sampleIds));
  }

  @Override
  public List<String> getDistinctEventTypes(String studyId) {
    return mapper.getDistinctEventTypes(studyId);
  }
}
