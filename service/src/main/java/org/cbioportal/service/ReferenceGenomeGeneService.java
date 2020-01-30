package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.ReferenceGenomeGene;

public interface ReferenceGenomeGeneService {
    List<ReferenceGenomeGene> fetchAllReferenceGenomeGenes(String genomeName);
    List<ReferenceGenomeGene> fetchGenesByGenomeName(
        List<Integer> geneIds,
        String genomeName
    );
    List<ReferenceGenomeGene> fetchGenesByHugoGeneSymbolsAndGenomeName(
        List<String> geneIds,
        String genomeName
    );
    ReferenceGenomeGene getReferenceGenomeGene(
        Integer geneID,
        String genomeName
    );
    ReferenceGenomeGene getReferenceGenomeGeneByEntityId(
        Integer entityId,
        String genomeName
    );
}
