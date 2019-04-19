package org.cbioportal.service.impl;


import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.persistence.ReferenceGenomeGeneRepository;
import org.cbioportal.service.ReferenceGenomeGeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReferenceGenomeGeneServiceImpl implements ReferenceGenomeGeneService {
    
    @Autowired
    private ReferenceGenomeGeneRepository referenceGenomeGeneRepository;
    
    @Override
    public List<ReferenceGenomeGene> fetchAllReferenceGenomeGenes(String genomeName) {
        
        return referenceGenomeGeneRepository.getAllGenesByGenomeName(genomeName);
    }

    @Override
    public List<ReferenceGenomeGene> fetchGenesByGenomeName(List<Integer> geneIds, String genomeName) {
        return referenceGenomeGeneRepository.getGenesByGenomeName(geneIds, genomeName);
    }

    @Override
    public List<ReferenceGenomeGene> fetchGenesByHugoGeneSymbolsAndGenomeName(List<String> geneIds, String genomeName) {
        return referenceGenomeGeneRepository.getGenesByHugoGeneSymbolsAndGenomeName(geneIds, genomeName);
    }
    
    @Override
    public ReferenceGenomeGene getReferenceGenomeGene(Integer geneId, String genomeName) {

        return referenceGenomeGeneRepository.getReferenceGenomeGene(geneId, genomeName);
    }

    @Override
    public ReferenceGenomeGene getReferenceGenomeGeneByEntityId(Integer entityId, String genomeName) {

        return referenceGenomeGeneRepository.getReferenceGenomeGeneByEntityId(entityId, genomeName);
    }
}
