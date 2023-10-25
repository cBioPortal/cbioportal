package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GenesetNotFoundException;

public interface GenesetService {

    List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber);

    BaseMeta getMetaGenesets();

    Geneset getGeneset(String genesetId) throws GenesetNotFoundException;
    
    List<Gene> getGenesByGenesetId(String genesetId) throws GenesetNotFoundException;
    
    List<Geneset> fetchGenesets(List<String> genesetIds);
    
    String getGenesetVersion();

}

