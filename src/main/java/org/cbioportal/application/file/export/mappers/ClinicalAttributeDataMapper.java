package org.cbioportal.application.file.export.mappers;

import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalPatientAttributeValue;
import org.cbioportal.application.file.model.ClinicalSampleAttributeValue;

import java.util.List;

public interface ClinicalAttributeDataMapper {

    List<ClinicalAttribute> getClinicalSampleAttributes(String studyId);

    Cursor<ClinicalSampleAttributeValue> getClinicalSampleAttributeValues(String studyId);

    List<ClinicalAttribute> getClinicalPatientAttributes(String studyId);

    Cursor<ClinicalPatientAttributeValue> getClinicalPatientAttributeValues(String studyId);

    boolean hasClinicalPatientAttributes(String studyId);

    boolean hasClinicalSampleAttributes(String studyId);
}
