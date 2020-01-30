package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBSample;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author abeshoua
 */
public interface SampleMapperLegacy {
    List<DBSample> getSamplesBySample(
        @Param("study_id") String study_id,
        @Param("sample_ids") List<String> sample_ids
    );
    List<DBSample> getSamplesByStudy(@Param("study_id") String study_id);
    List<DBSample> getSamplesByInternalId(
        @Param("sample_ids") List<String> sample_ids
    );
    List<DBSample> getSamplesByPatient(
        @Param("study_id") String study_id,
        @Param("patient_ids") List<String> patient_ids
    );
    List<Integer> getSampleInternalIdsByStudy(
        @Param("study_id") String study_id
    );
    List<Integer> getSampleInternalIdsBySample(
        @Param("study_id") String study_id,
        @Param("sample_ids") List<String> sample_ids
    );
}
