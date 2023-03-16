package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantQuery;
import org.cbioportal.webparam.GeneFilterQuery;
import org.springframework.cache.annotation.Cacheable;

public interface StructuralVariantRepository {


    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds,
                                                    List<String> sampleIds,
                                                    List<Integer> entrezGeneIds,
                                                    List<StructuralVariantQuery> structuralVariantQueries);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariantsByGeneQueries(List<String> molecularProfileIds,
                                                                 List<String> sampleIds,
                                                                 List<GeneFilterQuery> geneQueries);
}
