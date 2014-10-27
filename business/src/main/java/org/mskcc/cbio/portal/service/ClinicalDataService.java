/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBClinicalData;
import org.mskcc.cbio.portal.persistence.ClinicalDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class ClinicalDataService {
    @Autowired
    private ClinicalDataMapper clinicalDataMapper;
    @Transactional
    public List<DBClinicalData> byInternalStudyId(List<Integer> ids, boolean sample) {
        return (sample ? clinicalDataMapper.byInternalStudyId_Sample(ids) : clinicalDataMapper.byInternalStudyId_Patient(ids));
    }
    @Transactional
    public List<DBClinicalData> byStableStudyId(List<String> ids, boolean sample) {
        return (sample? clinicalDataMapper.byStableStudyId_Sample(ids) : clinicalDataMapper.byStableStudyId_Patient(ids));
    }
    @Transactional
    public List<DBClinicalData> byInternalSampleId(List<Integer> ids) {
        return clinicalDataMapper.byInternalSampleId(ids);
    }
    @Transactional
    public List<DBClinicalData> byInternalPatientId(List<Integer> ids) {
        return clinicalDataMapper.byInternalPatientId(ids);
    }
    @Transactional
    public List<DBClinicalData> byStableSampleId(String study_id, List<String> ids) {
        return clinicalDataMapper.byStableSampleIdStableStudyId(study_id, ids);
    }
    @Transactional
    public List<DBClinicalData> byStableSampleId(Integer study_id, List<String> ids) {
        return clinicalDataMapper.byStableSampleIdInternalStudyId(study_id, ids);
    }
    @Transactional
    public List<DBClinicalData> byStablePatientId(String study_id, List<String> ids) {
        return clinicalDataMapper.byStablePatientIdStableStudyId(study_id, ids);
    }
    @Transactional
    public List<DBClinicalData> byStablePatientId(Integer study_id, List<String> ids) {
        return clinicalDataMapper.byStablePatientIdInternalStudyId(study_id, ids);
    }
    
}
