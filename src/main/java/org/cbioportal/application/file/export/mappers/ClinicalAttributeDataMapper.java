package org.cbioportal.application.file.export.mappers;

import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributeValue;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;

import java.util.List;
import java.util.Set;

public interface ClinicalAttributeDataMapper {

    List<ClinicalAttribute> getClinicalSampleAttributes(String studyId);

    Cursor<ClinicalAttributeValue> getClinicalSampleAttributeValues(String studyId, Set<String> sampleIds);

    List<ClinicalAttribute> getClinicalPatientAttributes(String studyId);

    Cursor<ClinicalAttributeValue> getClinicalPatientAttributeValues(String studyId, Set<String> sampleIds);

    boolean hasClinicalPatientAttributes(String studyId, Set<String> sampleIds);

    boolean hasClinicalSampleAttributes(String studyId, Set<String> sampleIds);

    boolean hasClinicalTimelineData(String studyId, Set<String> sampleIds);

    List<String> getDistinctClinicalEventKeys(String studyId);

    Cursor<ClinicalEventData> getClinicalEventData(String studyId, Set<String> sampleIds);

    Cursor<ClinicalEvent> getClinicalEvents(String studyId, Set<String> sampleIds);
}
