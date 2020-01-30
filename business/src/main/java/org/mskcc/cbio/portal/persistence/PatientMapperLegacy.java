package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBPatient;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author abeshoua
 */
public interface PatientMapperLegacy {
    List<DBPatient> getPatientsByPatient(
        @Param("study_id") String study_id,
        @Param("patient_ids") List<String> patient_ids
    );
    List<DBPatient> getPatientsBySample(
        @Param("study_id") String study_id,
        @Param("sample_ids") List<String> sample_ids
    );
    List<DBPatient> getPatientsByStudy(@Param("study_id") String study_id);
    List<Integer> getPatientInternalIdsByStudy(
        @Param("study_id") String study_id
    );
    List<Integer> getPatientInternalIdsByPatient(
        @Param("study_id") String study_id,
        @Param("patient_ids") List<String> patient_ids
    );
}
