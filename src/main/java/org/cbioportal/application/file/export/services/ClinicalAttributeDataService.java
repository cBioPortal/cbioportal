package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributeValue;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

import java.util.List;

/**
 * Service to retrieve clinical data attributes and values for a study
 */
public class ClinicalAttributeDataService {

    private final ClinicalAttributeDataMapper clinicalAttributeDataMapper;

    public ClinicalAttributeDataService(ClinicalAttributeDataMapper clinicalAttributeDataMapper) {
        this.clinicalAttributeDataMapper = clinicalAttributeDataMapper;
    }

    public CloseableIterator<ClinicalAttributeValue> getClinicalSampleAttributeValues(String studyId) {
        return new CursorAdapter<>(clinicalAttributeDataMapper.getClinicalSampleAttributeValues(studyId));
    }

    public List<ClinicalAttribute> getClinicalSampleAttributes(String studyId) {
        return clinicalAttributeDataMapper.getClinicalSampleAttributes(studyId);
    }

    public CloseableIterator<ClinicalAttributeValue> getClinicalPatientAttributeValues(String studyId) {
        return new CursorAdapter<>(clinicalAttributeDataMapper.getClinicalPatientAttributeValues(studyId));
    }

    public List<ClinicalAttribute> getClinicalPatientAttributes(String studyId) {
        return clinicalAttributeDataMapper.getClinicalPatientAttributes(studyId);
    }

    public boolean hasClinicalPatientAttributes(String studyId) {
        return clinicalAttributeDataMapper.hasClinicalPatientAttributes(studyId);
    }

    public boolean hasClinicalSampleAttributes(String studyId) {
        return clinicalAttributeDataMapper.hasClinicalSampleAttributes(studyId);
    }
}
