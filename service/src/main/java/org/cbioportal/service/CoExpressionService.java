package org.cbioportal.service;

import org.cbioportal.model.CoExpression;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface CoExpressionService {
    
    List<CoExpression> getCoExpressions(String geneticEntityId, CoExpression.GeneticEntityType geneticEntityType, String sampleListId, String molecularProfileIdA, 
                                       String molecularProfileIdB, Double threshold) throws MolecularProfileNotFoundException, Exception;


    List<CoExpression> fetchCoExpressions(String geneticEntityId, CoExpression.GeneticEntityType geneticEntityType, List<String> sampleIds, String molecularProfileIdA, 
                                         String molecularProfileIdB, Double threshold) throws MolecularProfileNotFoundException, Exception;
}
