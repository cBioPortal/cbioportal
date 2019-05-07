package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

public interface GenesetRepository {

    @Cacheable("GeneralRepositoryCache")
    List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaGenesets();

    @Cacheable("GeneralRepositoryCache")
    Geneset getGeneset(String genesetId);
    
    @Cacheable("GeneralRepositoryCache")
    List<Geneset> fetchGenesets(List<String> genesetIds);
    
    @Cacheable("GeneralRepositoryCache")
    List<Gene> getGenesByGenesetId(String genesetId);
}
