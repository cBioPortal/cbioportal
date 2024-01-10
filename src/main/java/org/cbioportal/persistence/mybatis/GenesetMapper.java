package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;

public interface GenesetMapper {

    List<Geneset> getGenesets(String projection,
                        Integer limit,
                        Integer offset,
                        String sortBy,
                        String direction);

    BaseMeta getMetaGenesets();

    Geneset getGenesetByInternalId(Integer internalId,
            String projection);
    
    Geneset getGenesetByGenesetId(String genesetId,
                               String projection);

    Geneset getGenesetByGeneticEntityId(Integer geneticEntityId,
            String projection);

    List<Geneset> fetchGenesets(List<String> genesetIds);
    
    List<Gene> getGenesByGenesetId(String genesetId, String projection);
    
    String getGenesetVersion();
}
