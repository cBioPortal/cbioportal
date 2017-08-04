package org.cbioportal.service;

import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface MutationSpectrumService {
    
    List<MutationSpectrum> getMutationSpectrums(String geneticProfileId, String sampleListId) throws GeneticProfileNotFoundException;

    List<MutationSpectrum> fetchMutationSpectrums(String geneticProfileId, List<String> sampleIds) throws GeneticProfileNotFoundException;
}
