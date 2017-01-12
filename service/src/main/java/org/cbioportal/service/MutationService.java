package org.cbioportal.service;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface MutationService {
    
    List<Mutation> getMutationsInGeneticProfile(String geneticProfileId, String sampleId, String projection,
                                                Integer pageSize, Integer pageNumber, String sortBy, String direction);


    BaseMeta getMetaMutationsInGeneticProfile(String geneticProfileId, String sampleId);

    List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, String projection,
                                                  Integer pageSize, Integer pageNumber, String sortBy,
                                                  String direction);

    BaseMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds);
}
