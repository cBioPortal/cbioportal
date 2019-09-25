package org.cbioportal.service;

import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GeneNotFoundException;

import java.util.List;

public interface GeneService {

    List<Gene> getAllGenes(String keyword, String alias, String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                           String direction);

    BaseMeta getMetaGenes(String keyword, String alias);

    Gene getGene(String geneId) throws GeneNotFoundException;
    
    Gene getGeneByGeneticEntityId(Integer geneticEntityId) throws  GeneNotFoundException;

    List<String> getAliasesOfGene(String geneId) throws GeneNotFoundException;

    List<Gene> fetchGenes(List<String> geneIds, String geneIdType, String projection);

    BaseMeta fetchMetaGenes(List<String> geneIds, String geneIdType);
}
