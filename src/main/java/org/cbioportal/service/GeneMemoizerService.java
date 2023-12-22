package org.cbioportal.service;

import org.cbioportal.model.ReferenceGenomeGene;

import java.util.List;

public interface GeneMemoizerService {
    List<ReferenceGenomeGene> fetchGenes(String genomeName);
    
    void cacheGenes(List<ReferenceGenomeGene> genes, String genomeName); 
}
