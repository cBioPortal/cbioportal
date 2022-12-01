package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.StructVarFilterQuery;
import org.cbioportal.model.StructuralVariant;
import org.springframework.cache.annotation.Cacheable;

public interface StructuralVariantRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds,
                                                    List<String> sampleIds,
                                                    List<Integer> entrezGeneIds);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariantsByGeneQueries(List<String> molecularProfileIds,
                                                                 List<String> sampleIds,
                                                                 List<GeneFilterQuery> geneQueries);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariantsByStructVarQueries(List<String> molecularProfileIds,
                                                             List<String> sampleIds,
                                                             List<StructVarFilterQuery> structVarQueries);
}
