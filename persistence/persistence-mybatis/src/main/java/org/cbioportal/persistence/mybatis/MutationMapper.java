package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.meta.MutationMeta;

import java.util.List;

public interface MutationMapper {

    List<Mutation> getMutationsBySampleListId(String geneticProfileId, String sampleListId, List<Integer> entrezGeneIds,
                                              Boolean snpOnly, String projection, Integer limit, Integer offset, 
                                              String sortBy, String direction);

    MutationMeta getMetaMutationsBySampleListId(String geneticProfileId, String sampleListId, 
                                                List<Integer> entrezGeneIds, Boolean snpOnly);

    List<Mutation> getMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds, List<String> sampleIds,
                                                         List<Integer> entrezGeneIds, Boolean snpOnly,
                                                         String projection, Integer limit, Integer offset,
                                                         String sortBy, String direction);

    MutationMeta getMetaMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds, Boolean snpOnly);

    List<Mutation> getMutationsBySampleIds(String geneticProfileId, List<String> sampleIds, List<Integer> entrezGeneIds,
                                           Boolean snpOnly, String projection, Integer limit, Integer offset, 
                                           String sortBy, String direction);

    MutationMeta getMetaMutationsBySampleIds(String geneticProfileId, List<String> sampleIds, 
                                             List<Integer> entrezGeneIds, Boolean snpOnly);
    
    List<MutationCountByGene> getSampleCountByEntrezGeneIdsAndSampleIds(String geneticProfileId,
                                                                        List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds,
                                                                        Boolean snpOnly);

    List<MutationCountByGene> getPatientCountByEntrezGeneIdsAndSampleIds(String geneticProfileId,
                                                                         List<String> patientIds,
                                                                         List<Integer> entrezGeneIds,
                                                                         Boolean snpOnly);

    List<MutationCount> getMutationCountsBySampleListId(String geneticProfileId, String sampleListId);
    
    List<MutationCount> getMutationCountsBySampleIds(String geneticProfileId, List<String> sampleIds);
    
    MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart, 
                                                       Integer proteinPosEnd);
}
