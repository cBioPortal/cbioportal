/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.ArrayList;
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
    
    public List<DBPatientList> reduceLists(List<DBPatientList> list, boolean wholeLists) {
        // destructive
        if (wholeLists) {
            return list;
        } else {
            List<DBPatientList> ret = new ArrayList<>();
            for (DBPatientList pl: list) {
                ret.add(pl.discardList());
            }
            return ret;
        }
    }
                    
                    
    @Transactional
    public List<DBPatientList> byStableId(List<String> ids, boolean wholeLists) {
        return reduceLists(patientListMapper.byStableId(ids), wholeLists);
    }
    @Transactional
    public List<DBPatientList> byInternalId(List<Integer> ids, boolean wholeLists) {
        return reduceLists(patientListMapper.byInternalId(ids), wholeLists);
    }
    @Transactional
    public List<DBPatientList> byInternalStudyId(List<Integer> ids, boolean wholeLists) {
        return reduceLists(patientListMapper.byInternalStudyId(ids), wholeLists);
    }
    @Transactional
    public List<DBPatientList> getAll(boolean wholeLists) {
        return reduceLists(patientListMapper.getAll(), wholeLists);
    }
}
