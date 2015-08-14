/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBPatient;
import org.mskcc.cbio.portal.persistence.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author abeshoua
 */
@Service
public class PatientService {
    @Autowired
    private PatientMapper patientMapper;
    
    public List<DBPatient> byInternalStudyId(List<Integer> ids) {
        return patientMapper.byInternalStudyId(ids);
    }
    public List<DBPatient> byStableStudyId(List<String> ids) {
        return patientMapper.byStableStudyId(ids);
    }
    public List<DBPatient> byInternalPatientId(List<Integer> ids) {
        return patientMapper.byInternalPatientId(ids);
    }
    public List<DBPatient> byStablePatientId(Integer study, List<String> ids) {
        return patientMapper.byStablePatientIdInternalStudyId(study, ids);
    }
    public List<DBPatient> byStablePatientId(String study, List<String> ids) {
        return patientMapper.byStablePatientIdStableStudyId(study, ids);
    }
}
