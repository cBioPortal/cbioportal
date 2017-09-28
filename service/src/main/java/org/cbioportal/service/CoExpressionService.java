package org.cbioportal.service;

import org.cbioportal.model.CoExpression;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface CoExpressionService {
    
    List<CoExpression> getCoExpressions(String molecularProfileId, String sampleListId, Integer entrezGeneId, 
                                       Double threshold) throws MolecularProfileNotFoundException;


    List<CoExpression> fetchCoExpressions(String molecularProfileId, List<String> sampleIds, Integer entrezGeneId, 
                                         Double threshold) throws MolecularProfileNotFoundException;
}
