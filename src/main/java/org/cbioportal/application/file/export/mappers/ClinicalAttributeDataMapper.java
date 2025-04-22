package org.cbioportal.application.file.export.mappers;

import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributeValue;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;

import java.util.List;

public interface ClinicalAttributeDataMapper {

    List<ClinicalAttribute> getClinicalSampleAttributes(String studyId);

    Cursor<ClinicalAttributeValue> getClinicalSampleAttributeValues(String studyId);

    List<ClinicalAttribute> getClinicalPatientAttributes(String studyId);

    Cursor<ClinicalAttributeValue> getClinicalPatientAttributeValues(String studyId);

    boolean hasClinicalPatientAttributes(String studyId);

    boolean hasClinicalSampleAttributes(String studyId);

    boolean hasClinicalTimelineData(String studyId);

    List<String> getDistinctClinicalEventKeys(String studyId);

    Cursor<ClinicalEventData> getClinicalEventData(String studyId);

    Cursor<ClinicalEvent> getClinicalEvents(String studyId);
}
