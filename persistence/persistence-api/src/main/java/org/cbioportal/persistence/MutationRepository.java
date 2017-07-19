package org.cbioportal.persistence;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.meta.MutationMeta;

import java.util.List;

public interface MutationRepository {

    List<Mutation> getMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId, 
                                                              List<Integer> entrezGeneIds, Boolean snpOnly, 
                                                              String projection, Integer pageSize, Integer pageNumber, 
                                                              String sortBy, String direction);


    MutationMeta getMetaMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId, 
                                                                List<Integer> entrezGeneIds);

    List<Mutation> getMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds, List<String> sampleIds,
                                                         List<Integer> entrezGeneIds, String projection,
                                                         Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction);

    MutationMeta getMetaMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds);

    List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                  List<Integer> entrezGeneIds, Boolean snpOnly, String projection, 
                                                  Integer pageSize, Integer pageNumber, String sortBy, 
                                                  String direction);

    MutationMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                    List<Integer> entrezGeneIds);
    
    List<MutationCountByGene> getSampleCountByEntrezGeneIdsAndSampleIds(String geneticProfileId,
                                                                        List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds);

    List<MutationCountByGene> getPatientCountByEntrezGeneIdsAndSampleIds(String geneticProfileId,
                                                                         List<String> patientIds,
                                                                         List<Integer> entrezGeneIds);

    List<MutationCount> getMutationCountsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId);

    List<MutationCount> fetchMutationCountsInGeneticProfile(String geneticProfileId, List<String> sampleIds);

    MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart, 
                                                       Integer proteinPosEnd);
}
