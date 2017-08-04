package org.cbioportal.service;

import org.cbioportal.model.CoExpression;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface CoExpressionService {
    
    List<CoExpression> getCoExpressions(String geneticProfileId, String sampleListId, Integer entrezGeneId, 
                                       Double threshold) throws GeneticProfileNotFoundException;


    List<CoExpression> fetchCoExpressions(String geneticProfileId, List<String> sampleIds, Integer entrezGeneId, 
                                         Double threshold) throws GeneticProfileNotFoundException;
}
