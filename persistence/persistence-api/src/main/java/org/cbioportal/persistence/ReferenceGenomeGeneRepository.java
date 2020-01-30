package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.ReferenceGenomeGene;
import org.springframework.cache.annotation.Cacheable;

public interface ReferenceGenomeGeneRepository {
    @Cacheable(
        cacheNames = "StaticRepositoryCacheOne",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<ReferenceGenomeGene> getAllGenesByGenomeName(String genomeName);

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<ReferenceGenomeGene> getGenesByHugoGeneSymbolsAndGenomeName(
        List<String> geneIds,
        String genomeName
    );

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<ReferenceGenomeGene> getGenesByGenomeName(
        List<Integer> geneIds,
        String genomeName
    );

    @Cacheable(
        cacheNames = "StaticRepositoryCacheOne",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    ReferenceGenomeGene getReferenceGenomeGene(
        Integer geneId,
        String genomeName
    );

    @Cacheable(
        cacheNames = "StaticRepositoryCacheOne",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    ReferenceGenomeGene getReferenceGenomeGeneByEntityId(
        Integer geneticEntityId,
        String genomeName
    );
}
