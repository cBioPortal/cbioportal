package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.ReferenceGenomeGene;
import java.util.List;

public interface ReferenceGenomeGeneService {
    List<ReferenceGenomeGene> fetchAllReferenceGenomeGenes(String genomeName);
    List<ReferenceGenomeGene> fetchGenesByGenomeName(List<Integer> geneIds, String genomeName);
    List<ReferenceGenomeGene> fetchGenesByHugoGeneSymbolsAndGenomeName(List<String> geneIds, String genomeName);
    ReferenceGenomeGene getReferenceGenomeGene(Integer geneID, String genomeName);
    ReferenceGenomeGene getReferenceGenomeGeneByEntityId(Integer entityId, String genomeName);
}
