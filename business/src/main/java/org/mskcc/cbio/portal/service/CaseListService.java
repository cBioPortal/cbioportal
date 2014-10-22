/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBCaseList;
import org.mskcc.cbio.portal.persistence.CaseListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class CaseListService {
    @Autowired
    private CaseListMapper caseListMapper;
    
    @Transactional
    public List<DBCaseList> byStableId(List<String> ids) {
        return caseListMapper.byStableId(ids);
    }
    @Transactional
    public List<DBCaseList> byInternalId(List<Integer> ids) {
        return caseListMapper.byInternalId(ids);
    }
    @Transactional
    public List<DBCaseList> byInternalStudyId(List<Integer> ids) {
        return caseListMapper.byInternalStudyId(ids);
    }
    @Transactional
    public List<DBCaseList> getAll() {
        return caseListMapper.getAll();
    }
}
