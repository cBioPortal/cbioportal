package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.MutationSpectrum;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface MutationSpectrumService {
    
    List<MutationSpectrum> getMutationSpectrums(String molecularProfileId, String sampleListId) 
        throws MolecularProfileNotFoundException;

    List<MutationSpectrum> fetchMutationSpectrums(String molecularProfileId, List<String> sampleIds) 
        throws MolecularProfileNotFoundException;
}
