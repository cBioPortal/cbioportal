package org.cbioportal.persistence;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.StructuralVariant;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface StructuralVariantRepository {

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds, List<Integer> entrezGeneIds,
            List<String> sampleIds);
    
    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariantsByGeneQueries(List<String> molecularProfileIds, List<GeneFilterQuery> geneQueries,
            List<String> sampleIds);
}
