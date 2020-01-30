package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.ReferenceGenomeGene;

public interface GeneMemoizerService {
    List<ReferenceGenomeGene> fetchGenes(String genomeName);

    void cacheGenes(List<ReferenceGenomeGene> genes, String genomeName);
}
