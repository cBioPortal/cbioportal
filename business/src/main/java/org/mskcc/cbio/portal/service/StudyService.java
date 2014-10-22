/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBStudy;
import org.mskcc.cbio.portal.persistence.StudyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class StudyService {
    @Autowired
    private StudyMapper studyMapper;
    
    @Transactional
    public List<DBStudy> byStableId(List<String> ids) {
        return studyMapper.byStableId(ids);
    }
    @Transactional
    public List<DBStudy> byInternalId(List<Integer> ids) {
        return studyMapper.byInternalId(ids);
    }
    @Transactional
    public List<DBStudy> getAll() {
        return studyMapper.getAll();
    }
    
}
