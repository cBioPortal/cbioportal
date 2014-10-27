package org.mskcc.cbio.portal.persistence;


import java.util.List;
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
    List<DBClinicalData> byInternalStudyId_Sample(List<Integer> ids);
    List<DBClinicalData> byInternalStudyId_Patient(List<Integer> ids);
    List<DBClinicalData> byStableStudyId_Sample(List<String> ids);
    List<DBClinicalData> byStableStudyId_Patient(List<String> ids);
    List<DBClinicalData> byInternalSampleId(List<Integer> ids);
    List<DBClinicalData> byInternalPatientId(List<Integer> ids);
    List<DBClinicalData> byStableSampleIdInternalStudyId(Integer study, List<String> ids);
    List<DBClinicalData> byStablePatientIdInternalStudyId(Integer study, List<String> ids);
    List<DBClinicalData> byStableSampleIdStableStudyId(String study, List<String> ids);
    List<DBClinicalData> byStablePatientIdStableStudyId(String study, List<String> ids);
}
