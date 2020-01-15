package org.cbioportal.service;

import org.cbioportal.model.CoExpression;
import org.cbioportal.model.EntityType;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.service.exception.GenesetNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;

import java.util.List;

public interface CoExpressionService {

    List<CoExpression> getCoExpressions(String molecularProfileId, String sampleListId, String geneticEntityId, 
                                               EntityType geneticEntityType, Double threshold)
        throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException;
    
    List<CoExpression> getCoExpressions(String geneticEntityId, EntityType geneticEntityType, 
                                        String sampleListId, String molecularProfileIdA, String molecularProfileIdB, 
                                        Double threshold) 
        throws MolecularProfileNotFoundException, SampleListNotFoundException, GenesetNotFoundException, GeneNotFoundException;


    List<CoExpression> fetchCoExpressions(String geneticEntityId, EntityType geneticEntityType, 
                                          List<String> sampleIds, String molecularProfileIdA, String molecularProfileIdB, 
                                          Double threshold) 
        throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException;

    List<CoExpression> fetchCoExpressions(String molecularProfileId, List<String> sampleIds, String geneticEntityId, 
                                          EntityType geneticEntityType, Double threshold) 
        throws MolecularProfileNotFoundException ,GenesetNotFoundException, GeneNotFoundException;
}
