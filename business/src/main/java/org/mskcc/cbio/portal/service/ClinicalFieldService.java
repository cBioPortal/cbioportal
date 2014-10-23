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
    public List<DBClinicalField> byInternalStudyId(List<Integer> ids) {
        return clinicalFieldMapper.byInternalStudyId(ids);
    }
    @Transactional
    public List<DBClinicalField> byInternalCaseId(List<Integer> ids) {
        return clinicalFieldMapper.byInternalCaseId(ids);
    }
    
}
