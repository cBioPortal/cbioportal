/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBClinicalField;
import org.mskcc.cbio.portal.persistence.ClinicalFieldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class ClinicalFieldService {
    @Autowired
    private ClinicalFieldMapper clinicalFieldMapper;
    
    @Transactional
    public List<DBClinicalField> getAll() {
        return clinicalFieldMapper.getAll();
    }
    @Transactional
    public List<DBClinicalField> byInternalStudyId(List<Integer> ids, boolean sample) {
        return (sample ? clinicalFieldMapper.byInternalStudyId_Sample(ids) : clinicalFieldMapper.byInternalStudyId_Patient(ids));
    }
    @Transactional
    public List<DBClinicalField> byStableStudyId(List<String> ids, boolean sample) {
        return (sample? clinicalFieldMapper.byStableStudyId_Sample(ids) : clinicalFieldMapper.byStableStudyId_Patient(ids));
    }
    @Transactional
    public List<DBClinicalField> byInternalSampleId(List<Integer> ids) {
        return clinicalFieldMapper.byInternalSampleId(ids);
    }
    @Transactional
    public List<DBClinicalField> byInternalPatientId(List<Integer> ids) {
        return clinicalFieldMapper.byInternalPatientId(ids);
    }
    @Transactional
    public List<DBClinicalField> byStableSampleId(String study_id, List<String> ids) {
        return clinicalFieldMapper.byStableSampleIdStableStudyId(study_id, ids);
    }
    @Transactional
    public List<DBClinicalField> byStableSampleId(Integer study_id, List<String> ids) {
        return clinicalFieldMapper.byStableSampleIdInternalStudyId(study_id, ids);
    }
    @Transactional
    public List<DBClinicalField> byStablePatientId(String study_id, List<String> ids) {
        return clinicalFieldMapper.byStablePatientIdStableStudyId(study_id, ids);
    }
    @Transactional
    public List<DBClinicalField> byStablePatientId(Integer study_id, List<String> ids) {
        return clinicalFieldMapper.byStablePatientIdInternalStudyId(study_id, ids);
    }
    
}
