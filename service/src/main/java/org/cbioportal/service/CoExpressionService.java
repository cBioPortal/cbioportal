package org.cbioportal.service;

import org.cbioportal.model.CoExpression;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.service.exception.GenesetNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;

import java.util.List;

public interface CoExpressionService {

    List<CoExpression> getCoExpressions(String molecularProfileId, String sampleListId, String geneticEntityId, 
                                               CoExpression.GeneticEntityType geneticEntityType, Double threshold)
        throws MolecularProfileNotFoundException;
    
    List<CoExpression> getCoExpressions(String geneticEntityId, CoExpression.GeneticEntityType geneticEntityType, 
                                        String sampleListId, String molecularProfileIdA, String molecularProfileIdB, 
                                        Double threshold) 
        throws MolecularProfileNotFoundException, SampleListNotFoundException, GenesetNotFoundException, GeneNotFoundException;


    List<CoExpression> fetchCoExpressions(String geneticEntityId, CoExpression.GeneticEntityType geneticEntityType, 
                                          List<String> sampleIds, String molecularProfileIdA, String molecularProfileIdB, 
                                          Double threshold) 
        throws MolecularProfileNotFoundException, Exception;

    List<CoExpression> fetchCoExpressions(String molecularProfileId, List<String> sampleIds, String geneticEntityId, 
                                          CoExpression.GeneticEntityType geneticEntityType, Double threshold) 
        throws MolecularProfileNotFoundException;
}
