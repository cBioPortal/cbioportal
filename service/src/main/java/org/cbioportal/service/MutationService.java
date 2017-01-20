package org.cbioportal.service;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.model.MutationSampleCountByKeyword;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.model.meta.MutationMeta;

import java.util.List;

public interface MutationService {
    
    List<Mutation> getMutationsInGeneticProfile(String geneticProfileId, String sampleId, String projection,
                                                Integer pageSize, Integer pageNumber, String sortBy, String direction);


    MutationMeta getMetaMutationsInGeneticProfile(String geneticProfileId, String sampleId);

    List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, String projection,
                                                  Integer pageSize, Integer pageNumber, String sortBy,
                                                  String direction);

    MutationMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds);

    List<MutationSampleCountByGene> getSampleCountByEntrezGeneIds(String geneticProfileId, List<Integer> entrezGeneIds);

    List<MutationSampleCountByKeyword> getSampleCountByKeywords(String geneticProfileId, List<String> keywords);
}
