package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.GeneNotFoundException;
import org.cbioportal.legacy.service.exception.GeneWithMultipleEntrezIdsException;

import java.util.List;

public interface GeneService {

    List<Gene> getAllGenes(String keyword, String alias, String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                           String direction);

    BaseMeta getMetaGenes(String keyword, String alias);

    Gene getGene(String geneId) throws GeneNotFoundException, GeneWithMultipleEntrezIdsException;
    
    Gene getGeneByGeneticEntityId(Integer geneticEntityId) throws  GeneNotFoundException;

    List<String> getAliasesOfGene(String geneId) throws GeneNotFoundException, GeneWithMultipleEntrezIdsException;

    List<Gene> fetchGenes(List<String> geneIds, String geneIdType, String projection);

    BaseMeta fetchMetaGenes(List<String> geneIds, String geneIdType);
}
