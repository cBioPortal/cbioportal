package org.cbioportal.persistence;

import org.cbioportal.model.ReferenceGenomeGene;
import java.util.List;

public interface ReferenceGenomeGeneRepository {
    List<ReferenceGenomeGene> getAllGenesByGenomeName(String genomeName);
    List<ReferenceGenomeGene> getGenesByHugoGeneSymbolsAndGenomeName(List<String> geneIds, String genomeName);
    List<ReferenceGenomeGene> getGenesByGenomeName(List<Integer> geneIds, String genomeName);
    ReferenceGenomeGene getReferenceGenomeGene(Integer geneId, String genomeName);
    ReferenceGenomeGene getReferenceGenomeGeneByEntityId(Integer geneticEntityId, String genomeName);
}