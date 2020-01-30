package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.ReferenceGenomeGene;

public interface ReferenceGenomeGeneMapper {
    List<ReferenceGenomeGene> getAllGenesByGenomeName(
        String genomeName,
        String projection
    );
    List<ReferenceGenomeGene> getGenesByHugoGeneSymbolsAndGenomeName(
        List<String> geneIds,
        String genomeName,
        String projection
    );
    List<ReferenceGenomeGene> getGenesByGenomeName(
        List<Integer> geneIds,
        String genomeName,
        String projection
    );
    ReferenceGenomeGene getReferenceGenomeGene(
        Integer geneId,
        String genomeName,
        String projection
    );
    ReferenceGenomeGene getReferenceGenomeGeneByEntityId(
        Integer geneticEntityId,
        String genomeName,
        String projection
    );
}
