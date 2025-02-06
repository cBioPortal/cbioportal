package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.ReferenceGenomeGene;

import java.util.List;

public interface GeneMemoizerService {
    List<ReferenceGenomeGene> fetchGenes(String genomeName);
    
    void cacheGenes(List<ReferenceGenomeGene> genes, String genomeName); 
}
