package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

public interface GenesetRepository {

    @Cacheable("RepositoryCache")
    List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaGenesets();

    @Cacheable("RepositoryCache")
    Geneset getGeneset(String genesetId);
    
    @Cacheable("RepositoryCache")
    List<Geneset> fetchGenesets(List<String> genesetIds);
    
    @Cacheable("RepositoryCache")
    List<Gene> getGenesByGenesetId(String genesetId);
}
