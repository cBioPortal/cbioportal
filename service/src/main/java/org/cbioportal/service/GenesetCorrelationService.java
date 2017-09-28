package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenesetCorrelation;
import org.cbioportal.service.exception.GenesetNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;

public interface GenesetCorrelationService {
    
	List<GenesetCorrelation> fetchCorrelatedGenes(String genesetId, String geneticProfileId, 
			double correlationThreshold) throws MolecularProfileNotFoundException, GenesetNotFoundException;
	
	List<GenesetCorrelation> fetchCorrelatedGenes(String genesetId, String geneticProfileId, 
			List<String> sampleIds, double correlationThreshold) throws MolecularProfileNotFoundException, GenesetNotFoundException;

	List<GenesetCorrelation> fetchCorrelatedGenes(String genesetId, String geneticProfileId, String sampleListId,
			double correlationThreshold) throws MolecularProfileNotFoundException, SampleListNotFoundException, GenesetNotFoundException;
}
