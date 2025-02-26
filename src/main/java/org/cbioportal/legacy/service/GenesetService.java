package org.cbioportal.legacy.service;

import java.util.List;

import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.Geneset;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.GenesetNotFoundException;

public interface GenesetService {

    List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber);

    BaseMeta getMetaGenesets();

    Geneset getGeneset(String genesetId) throws GenesetNotFoundException;
    
    List<Gene> getGenesByGenesetId(String genesetId) throws GenesetNotFoundException;
    
    List<Geneset> fetchGenesets(List<String> genesetIds);
    
    String getGenesetVersion();

}

