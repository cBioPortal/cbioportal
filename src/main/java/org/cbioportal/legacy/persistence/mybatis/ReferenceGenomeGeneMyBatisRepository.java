package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;

import org.cbioportal.legacy.model.ReferenceGenomeGene;
import org.cbioportal.legacy.persistence.ReferenceGenomeGeneRepository;
import org.cbioportal.legacy.persistence.PersistenceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ReferenceGenomeGeneMyBatisRepository implements ReferenceGenomeGeneRepository {

    @Autowired
    private ReferenceGenomeGeneMapper referenceGenomeGeneMapper;

    @Override
    public List<ReferenceGenomeGene> getAllGenesByGenomeName(String genomeName) {

        return referenceGenomeGeneMapper.getAllGenesByGenomeName(genomeName, PersistenceConstants.SUMMARY_PROJECTION);
    }

    @Override
    public List<ReferenceGenomeGene> getGenesByHugoGeneSymbolsAndGenomeName(List<String> geneIds, String genomeName) {

        return referenceGenomeGeneMapper.getGenesByHugoGeneSymbolsAndGenomeName(geneIds, genomeName, PersistenceConstants.SUMMARY_PROJECTION);
    }

    @Override
    public List<ReferenceGenomeGene> getGenesByGenomeName(List<Integer> geneIds, String genomeName) {

        return referenceGenomeGeneMapper.getGenesByGenomeName(geneIds, genomeName, PersistenceConstants.SUMMARY_PROJECTION);
    }
    
    @Override
    public ReferenceGenomeGene getReferenceGenomeGene(Integer geneId, String genomeName) {

        return referenceGenomeGeneMapper.getReferenceGenomeGene(geneId, genomeName, PersistenceConstants.SUMMARY_PROJECTION);
    }
    
    @Override
    public ReferenceGenomeGene getReferenceGenomeGeneByEntityId(Integer geneticEntityId, String genomeName) {
        return referenceGenomeGeneMapper.getReferenceGenomeGeneByEntityId(geneticEntityId, genomeName, 
                                    PersistenceConstants.SUMMARY_PROJECTION);
    }
}
