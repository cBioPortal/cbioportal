package org.mskcc.cbio.portal.persistence;


import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBClinicalData;

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
    List<DBClinicalData> byInternalStudyId_Sample(@Param("ids") List<Integer> ids);
    List<DBClinicalData> byInternalStudyId_Patient(@Param("ids") List<Integer> ids);
    List<DBClinicalData> byStableStudyId_Sample(@Param("ids") List<String> ids);
    List<DBClinicalData> byStableStudyId_Patient(@Param("ids") List<String> ids);
    List<DBClinicalData> byInternalSampleId(@Param("ids") List<Integer> ids);
    List<DBClinicalData> byInternalPatientId(@Param("ids") List<Integer> ids);
    List<DBClinicalData> byStableSampleIdInternalStudyId(@Param("study") Integer study, @Param("ids") List<String> ids);
    List<DBClinicalData> byStablePatientIdInternalStudyId(@Param("study") Integer study, @Param("ids") List<String> ids);
    List<DBClinicalData> byStableSampleIdStableStudyId(@Param("study") String study, @Param("ids") List<String> ids);
    List<DBClinicalData> byStablePatientIdStableStudyId(@Param("study") String study, @Param("ids") List<String> ids);
}
