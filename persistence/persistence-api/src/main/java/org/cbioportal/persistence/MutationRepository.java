package org.cbioportal.persistence;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.meta.MutationMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface MutationRepository {

    @Cacheable("GeneralRepositoryCache")
    List<Mutation> getMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                String projection, Integer pageSize, Integer pageNumber,
                                                                String sortBy, String direction);


    @Cacheable("GeneralRepositoryCache")
    MutationMeta getMetaMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                  List<Integer> entrezGeneIds);

    @Cacheable("GeneralRepositoryCache")
    List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds, String projection,
                                                           Integer pageSize, Integer pageNumber,
                                                           String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds,
                                                             List<Integer> entrezGeneIds);

    @Cacheable("GeneralRepositoryCache")
    List<Mutation> fetchMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                    List<Integer> entrezGeneIds, Boolean snpOnly, String projection,
                                                    Integer pageSize, Integer pageNumber, String sortBy,
                                                    String direction);

    @Cacheable("GeneralRepositoryCache")
    MutationMeta fetchMetaMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                      List<Integer> entrezGeneIds);

    @Cacheable("GeneralRepositoryCache")
    List<MutationCountByGene> getSampleCountByEntrezGeneIdsAndSampleIds(String molecularProfileId,
                                                                        List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds);

    @Cacheable("GeneralRepositoryCache")
    List<MutationCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                        List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds);

    @Cacheable("GeneralRepositoryCache")
    List<MutationCountByGene> getPatientCountByEntrezGeneIdsAndSampleIds(String molecularProfileId,
                                                                         List<String> patientIds,
                                                                         List<Integer> entrezGeneIds);

    @Cacheable("GeneralRepositoryCache")
    MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart, 
                                                       Integer proteinPosEnd);
}
