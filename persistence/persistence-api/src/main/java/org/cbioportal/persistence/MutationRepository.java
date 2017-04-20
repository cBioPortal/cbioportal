package org.cbioportal.persistence;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.model.MutationSampleCountByKeyword;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.model.meta.MutationMeta;

import java.util.List;

public interface MutationRepository {

    List<Mutation> getMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId, 
                                                              List<Integer> entrezGeneIds, String projection, 
                                                              Integer pageSize, Integer pageNumber, String sortBy, 
                                                              String direction);


    MutationMeta getMetaMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId, 
                                                                List<Integer> entrezGeneIds);

    List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                  List<Integer> entrezGeneIds, String projection, Integer pageSize, 
                                                  Integer pageNumber, String sortBy, String direction);

    MutationMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                    List<Integer> entrezGeneIds);

    List<MutationSampleCountByGene> getSampleCountByEntrezGeneIds(String geneticProfileId, List<Integer> entrezGeneIds);

    List<MutationSampleCountByKeyword> getSampleCountByKeywords(String geneticProfileId, List<String> keywords);

    List<MutationCount> getMutationCountsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId);

    List<MutationCount> fetchMutationCountsInGeneticProfile(String geneticProfileId, List<String> sampleIds);
}
