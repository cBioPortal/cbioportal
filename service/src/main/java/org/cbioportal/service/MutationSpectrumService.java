package org.cbioportal.service;

import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface MutationSpectrumService {
    
    List<MutationSpectrum> getMutationSpectrums(String molecularProfileId, String sampleListId) 
        throws MolecularProfileNotFoundException;

    List<MutationSpectrum> fetchMutationSpectrums(String molecularProfileId, List<String> sampleIds) 
        throws MolecularProfileNotFoundException;
}
