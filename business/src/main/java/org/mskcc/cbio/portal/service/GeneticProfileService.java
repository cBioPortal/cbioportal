/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBGeneticProfile;
import org.mskcc.cbio.portal.persistence.GeneticProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class GeneticProfileService {
    @Autowired
    private GeneticProfileMapper geneticProfileMapper;
    
    @Transactional
    public List<DBGeneticProfile> byStableId(List<String> ids) {
        return geneticProfileMapper.byStableId(ids);
    }
    @Transactional
    public List<DBGeneticProfile> byInternalId(List<Integer> ids) {
        return geneticProfileMapper.byInternalId(ids);
    }
    @Transactional
    public List<DBGeneticProfile> byInternalStudyId(List<Integer> ids) {
        return geneticProfileMapper.byInternalStudyId(ids);
    }
    @Transactional
    public List<DBGeneticProfile> getAll() {
        return geneticProfileMapper.getAll();
    }
    
}
