/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBGene;
import org.mskcc.cbio.portal.persistence.GeneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Service
public class GeneService {
    @Autowired
    private GeneMapper geneMapper;
    
    @Transactional
    public List<DBGene> byEntrezGeneId(List<Long> ids) {
        return geneMapper.byEntrezGeneId(ids);
    }
    @Transactional
    public List<DBGene> byHugoGeneSymbol(List<String> ids) {
        return geneMapper.byHugoGeneSymbol(ids);
    }
}
