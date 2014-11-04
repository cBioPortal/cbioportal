package org.mskcc.cbio.portal.persistence;


import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBClinicalPatientData;
import org.mskcc.cbio.portal.model.DBClinicalSampleData;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author abeshoua
 */
public interface ClinicalDataMapper {
    List<DBClinicalSampleData> byInternalStudyId_Sample(@Param("ids") List<Integer> ids);
    List<DBClinicalPatientData> byInternalStudyId_Patient(@Param("ids") List<Integer> ids);
    List<DBClinicalSampleData> byStableStudyId_Sample(@Param("ids") List<String> ids);
    List<DBClinicalPatientData> byStableStudyId_Patient(@Param("ids") List<String> ids);
    List<DBClinicalSampleData> byInternalSampleId(@Param("ids") List<Integer> ids);
    List<DBClinicalPatientData> byInternalPatientId(@Param("ids") List<Integer> ids);
    List<DBClinicalSampleData> byStableSampleIdInternalStudyId(@Param("study") Integer study, @Param("ids") List<String> ids);
    List<DBClinicalPatientData> byStablePatientIdInternalStudyId(@Param("study") Integer study, @Param("ids") List<String> ids);
    List<DBClinicalSampleData> byStableSampleIdStableStudyId(@Param("study") String study, @Param("ids") List<String> ids);
    List<DBClinicalPatientData> byStablePatientIdStableStudyId(@Param("study") String study, @Param("ids") List<String> ids);
}
