/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBPatientList;
import org.mskcc.cbio.portal.persistence.PatientListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class PatientListService {
    @Autowired
    private PatientListMapper patientListMapper;
    
    @Transactional
    public List<DBPatientList> byStableId(List<String> ids) {
        return patientListMapper.byStableId(ids);
    }
    @Transactional
    public List<DBPatientList> byInternalId(List<Integer> ids) {
        return patientListMapper.byInternalId(ids);
    }
    @Transactional
    public List<DBPatientList> byInternalStudyId(List<Integer> ids) {
        return patientListMapper.byInternalStudyId(ids);
    }
    @Transactional
    public List<DBPatientList> getAll() {
        return patientListMapper.getAll();
    }
}
