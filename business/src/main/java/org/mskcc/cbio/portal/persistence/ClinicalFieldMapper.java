package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBClinicalField;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author abeshoua
 */
public interface ClinicalFieldMapper {
    List<DBClinicalField> getSampleClinicalFieldsByStudy(
        @Param("study_id") Integer study_id
    );
    List<DBClinicalField> getPatientClinicalFieldsByStudy(
        @Param("study_id") Integer study_id
    );

    List<DBClinicalField> getSampleClinicalFieldsBySample(
        @Param("study_id") String study_id,
        @Param("sample_ids") List<String> sample_ids
    );
    List<DBClinicalField> getPatientClinicalFieldsByPatient(
        @Param("study_id") String study_id,
        @Param("patient_ids") List<String> patient_ids
    );

    List<DBClinicalField> getSampleClinicalFieldsBySampleInternalIds(
        @Param("sample_ids") List<Integer> sample_ids
    );
    List<DBClinicalField> getPatientClinicalFieldsByPatientInternalIds(
        @Param("patient_ids") List<Integer> patient_ids
    );

    List<DBClinicalField> getAllSampleClinicalFields();
    List<DBClinicalField> getAllPatientClinicalFields();

    List<DBClinicalField> getAllClinicalFields();
    List<DBClinicalField> getAllClinicalFieldsByStudy(
        @Param("study_id") Integer study_id
    );
    List<DBClinicalField> getClinicalFieldsById(
        @Param("attr_ids") List<String> attr_ids
    );
}
