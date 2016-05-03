package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.DBGene;
import org.mskcc.cbio.portal.persistence.GeneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author zheins
 */
@Service
public class GeneService {
    @Autowired
    private GeneMapper geneMapper;
    
    @Transactional
    public List<DBGene> byEntrezGeneId(List<Long> entrezIds) {
        return geneMapper.byEntrezGeneId(entrezIds);
    }
    @Transactional
    public List<DBGene> byHugoGeneSymbol(List<String> hugoGeneSymbols) {
        return geneMapper.byHugoGeneSymbol(hugoGeneSymbols);
    }
    @Transactional
    public List<DBGene> getAll() {
        return geneMapper.getAll();
    }
}
