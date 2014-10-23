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
    public List<DBClinicalData> byInternalStudyId(List<Integer> ids) {
        return clinicalDataMapper.byInternalStudyId(ids);
    }
    @Transactional
    public List<DBClinicalData> byInternalCaseId(List<Integer> ids) {
        return clinicalDataMapper.byInternalCaseId(ids);
    }
}
