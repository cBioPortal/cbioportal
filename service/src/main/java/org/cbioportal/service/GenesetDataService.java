package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;

public interface GenesetDataService {

    List<GenesetMolecularData> fetchGenesetData(String geneticProfileId, List<String> sampleIds, List<String> genesetIds) throws MolecularProfileNotFoundException;

    List<GenesetMolecularData> fetchGenesetData(String geneticProfileId, String sampleListId, List<String> genesetIds) throws MolecularProfileNotFoundException, SampleListNotFoundException;
    
    List<GenesetMolecularAlteration> getGenesetAlterations(String molecularProfileId, List<String> genesetIds) throws MolecularProfileNotFoundException;
}
