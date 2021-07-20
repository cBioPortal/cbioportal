package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.StructuralVariant;
import org.springframework.cache.annotation.Cacheable;

public interface StructuralVariantRepository {

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds,
                                                    List<String> sampleIds,
                                                    List<Integer> entrezGeneIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariantsByGeneQueries(List<String> molecularProfileIds,
                                                                 List<String> sampleIds,
                                                                 List<GeneFilterQuery> geneQueries);
}
