package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ReferenceGenomeGene;
import java.util.List;

public interface ReferenceGenomeGeneMapper {
    List<ReferenceGenomeGene> getAllGenesByGenomeName(String genomeName, String projection);
    List<ReferenceGenomeGene> getGenesByHugoGeneSymbolsAndGenomeName(List<String> geneIds, String genomeName, String projection);
    List<ReferenceGenomeGene> getGenesByGenomeName(List<Integer> geneIds, String genomeName, String projection);
    ReferenceGenomeGene getReferenceGenomeGene(Integer geneId, String genomeName, String projection);
    ReferenceGenomeGene getReferenceGenomeGeneByEntityId(Integer geneticEntityId, String genomeName, String projection);
}

