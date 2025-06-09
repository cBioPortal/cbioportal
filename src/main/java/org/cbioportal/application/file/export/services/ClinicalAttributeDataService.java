package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributeValue;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

import java.util.List;
import java.util.Set;

/**
 * Service to retrieve clinical data attributes and values for a study
 */
public class ClinicalAttributeDataService {

    private final ClinicalAttributeDataMapper clinicalAttributeDataMapper;

    public ClinicalAttributeDataService(ClinicalAttributeDataMapper clinicalAttributeDataMapper) {
        this.clinicalAttributeDataMapper = clinicalAttributeDataMapper;
    }

    public CloseableIterator<ClinicalAttributeValue> getClinicalSampleAttributeValues(String studyId, Set<String> sampleIds) {
        return new CursorAdapter<>(clinicalAttributeDataMapper.getClinicalSampleAttributeValues(studyId, sampleIds));
    }

    public List<ClinicalAttribute> getClinicalSampleAttributes(String studyId) {
        return clinicalAttributeDataMapper.getClinicalSampleAttributes(studyId);
    }

    public CloseableIterator<ClinicalAttributeValue> getClinicalPatientAttributeValues(String studyId, Set<String> sampleIds) {
        return new CursorAdapter<>(clinicalAttributeDataMapper.getClinicalPatientAttributeValues(studyId, sampleIds));
    }

    public List<ClinicalAttribute> getClinicalPatientAttributes(String studyId) {
        return clinicalAttributeDataMapper.getClinicalPatientAttributes(studyId);
    }

    public boolean hasClinicalPatientAttributes(String studyId, Set<String> sampleIds) {
        return clinicalAttributeDataMapper.hasClinicalPatientAttributes(studyId, sampleIds);
    }

    public boolean hasClinicalSampleAttributes(String studyId, Set<String> sampleIds) {
        return clinicalAttributeDataMapper.hasClinicalSampleAttributes(studyId, sampleIds);
    }

    public boolean hasClinicalTimelineData(String studyId, Set<String> sampleIds) {
        return clinicalAttributeDataMapper.hasClinicalTimelineData(studyId, sampleIds);
    }

    public List<String> getDistinctClinicalEventKeys(String studyId, String eventType) {
        return clinicalAttributeDataMapper.getDistinctClinicalEventKeys(studyId, eventType);
    }

    public CloseableIterator<ClinicalEventData> getClinicalEventData(String studyId, String eventType, Set<String> sampleIds) {
        return new CursorAdapter<>(clinicalAttributeDataMapper.getClinicalEventData(studyId, eventType, sampleIds));
    }

    public CloseableIterator<ClinicalEvent> getClinicalEvents(String studyId, String eventType, Set<String> sampleIds) {
        return new CursorAdapter<>(clinicalAttributeDataMapper.getClinicalEvents(studyId, eventType, sampleIds));
    }

    public List<String> getDistinctEventTypes(String studyId) {
        return clinicalAttributeDataMapper.getDistinctEventTypes(studyId);
    }
}
