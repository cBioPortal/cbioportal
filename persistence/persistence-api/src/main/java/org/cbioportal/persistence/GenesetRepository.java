package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;

public interface GenesetRepository {

    List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber);

    BaseMeta getMetaGenesets();

    Geneset getGeneset(String genesetId);
    
    List<Geneset> fetchGenesets(List<String> genesetIds);
    
    List<Gene> getGenesByGenesetId(String genesetId);
}
