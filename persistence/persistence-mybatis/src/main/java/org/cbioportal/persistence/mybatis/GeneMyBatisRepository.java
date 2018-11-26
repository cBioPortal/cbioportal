package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GeneMyBatisRepository implements GeneRepository {

    @Autowired
    private GeneMapper geneMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Gene> getAllGenes(String keyword, String alias, String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                  String direction) {

        return geneMapper.getGenes(keyword, alias, projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), 
            sortBy, direction);
    }

    @Override
    public BaseMeta getMetaGenes(String keyword, String alias) {

        return geneMapper.getMetaGenes(keyword, alias);
    }

    @Override
    public Gene getGeneByEntrezGeneId(Integer entrezGeneId) {

        return geneMapper.getGeneByEntrezGeneId(entrezGeneId, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public Gene getGeneByHugoGeneSymbol(String hugoGeneSymbol) {

        return geneMapper.getGeneByHugoGeneSymbol(hugoGeneSymbol, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<String> getAliasesOfGeneByEntrezGeneId(Integer entrezGeneId) {

        return geneMapper.getAliasesOfGeneByEntrezGeneId(entrezGeneId);
    }

    @Override
    public List<String> getAliasesOfGeneByHugoGeneSymbol(String hugoGeneSymbol) {

        return geneMapper.getAliasesOfGeneByHugoGeneSymbol(hugoGeneSymbol);
    }


    @Override
    public List<Gene> fetchGenesByEntrezGeneIds(List<Integer> entrezGeneIds, String projection) {

        return geneMapper.getGenesByEntrezGeneIds(entrezGeneIds, projection);
    }

    @Override
    public List<Gene> fetchGenesByHugoGeneSymbols(List<String> hugoGeneSymbols, String projection) {

        return geneMapper.getGenesByHugoGeneSymbols(hugoGeneSymbols, projection);
    }

    @Override
    public BaseMeta fetchMetaGenesByEntrezGeneIds(List<Integer> entrezGeneIds) {

        return geneMapper.getMetaGenesByEntrezGeneIds(entrezGeneIds);
    }

    @Override
    public BaseMeta fetchMetaGenesByHugoGeneSymbols(List<String> hugoGeneSymbols) {

        return geneMapper.getMetaGenesByHugoGeneSymbols(hugoGeneSymbols);
    }
}
