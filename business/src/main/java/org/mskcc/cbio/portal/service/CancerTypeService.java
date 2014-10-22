/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBCancerType;
import org.mskcc.cbio.portal.model.DBGene;
import org.mskcc.cbio.portal.persistence.CancerTypeMapper;
import org.mskcc.cbio.portal.persistence.GeneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class CancerTypeService {
    @Autowired
    private CancerTypeMapper cancerTypeMapper;
    @Transactional
    public List<DBCancerType> getAll() {
        return cancerTypeMapper.getAll();
    }
    @Transactional
    public List<DBCancerType> byId(List<String> ids) {
        return cancerTypeMapper.byId(ids);
    }
}