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
    List<DBClinicalField> byInternalStudyId_Sample(@Param("ids") List<Integer> ids);
    List<DBClinicalField> byInternalStudyId_Patient(@Param("ids") List<Integer> ids);
    List<DBClinicalField> byStableStudyId_Sample(@Param("ids") List<String> ids);
    List<DBClinicalField> byStableStudyId_Patient(@Param("ids") List<String> ids);
    List<DBClinicalField> byInternalSampleId(@Param("ids") List<Integer> ids);
    List<DBClinicalField> byInternalPatientId(@Param("ids") List<Integer> ids);
    List<DBClinicalField> byStableSampleIdStableStudyId(@Param("study_id") String study_id, @Param("ids") List<String> ids);
    List<DBClinicalField> byStableSampleIdInternalStudyId(@Param("study_id") Integer study_id, @Param("ids") List<String> ids);
    List<DBClinicalField> byStablePatientIdStableStudyId(@Param("study_id") String study_id, @Param("ids") List<String> ids);
    List<DBClinicalField> byStablePatientIdInternalStudyId(@Param("study_id") Integer study_id, @Param("ids") List<String> ids);
    List<DBClinicalField> getAll();
}
