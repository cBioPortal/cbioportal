package org.cbioportal.service;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface MutationService {
    
    List<Mutation> getMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId, 
                                                              List<Integer> entrezGeneIds, Boolean snpOnly,
                                                              String projection, Integer pageSize, Integer pageNumber,
                                                              String sortBy, String direction) 
        throws GeneticProfileNotFoundException;

    MutationMeta getMetaMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId, 
                                                                List<Integer> entrezGeneIds) 
        throws GeneticProfileNotFoundException;

    List<Mutation> getMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds, List<String> sampleIds,
                                                         List<Integer> entrezGeneIds, String projection,
                                                         Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction);

    MutationMeta getMetaMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds);

    List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                  List<Integer> entrezGeneIds, Boolean snpOnly, String projection, 
                                                  Integer pageSize, Integer pageNumber, String sortBy, String direction) 
        throws GeneticProfileNotFoundException;

    MutationMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                    List<Integer> entrezGeneIds) throws GeneticProfileNotFoundException;

    List<MutationCountByGene> getSampleCountByEntrezGeneIdsAndSampleIds(String geneticProfileId,
                                                                        List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException;

    List<MutationCountByGene> getPatientCountByEntrezGeneIdsAndSampleIds(String geneticProfileId,
                                                                        List<String> patientIds,
                                                                        List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException;

    List<MutationCount> getMutationCountsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId) 
        throws GeneticProfileNotFoundException;

    List<MutationCount> fetchMutationCountsInGeneticProfile(String geneticProfileId, List<String> sampleIds) 
        throws GeneticProfileNotFoundException;

    List<MutationCountByPosition> fetchMutationCountsByPosition(List<Integer> entrezGeneIds, 
                                                                List<Integer> proteinPosStarts, 
                                                                List<Integer> proteinPosEnds);
}
