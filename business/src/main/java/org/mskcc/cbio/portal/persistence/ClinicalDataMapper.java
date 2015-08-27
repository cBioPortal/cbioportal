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
	List<DBClinicalSampleData> getSampleClinicalDataByStudy(@Param("study_id") String study_id);
	List<DBClinicalPatientData> getPatientClinicalDataByStudy(@Param("study_id") String study_id);
	
	List<DBClinicalSampleData> getSampleClinicalDataBySample(@Param("study_id") String study_id, @Param("sample_ids") List<String> sample_ids);
	List<DBClinicalPatientData> getPatientClinicalDataByPatient(@Param("study_id") String study_id, @Param("patient_ids") List<String> patient_ids);
}
