package org.cbioportal.persistence;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.meta.MutationMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface MutationRepository {

    @Cacheable("RepositoryCache")
    List<Mutation> getMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                String projection, Integer pageSize, Integer pageNumber,
                                                                String sortBy, String direction);


    @Cacheable("RepositoryCache")
    MutationMeta getMetaMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                  List<Integer> entrezGeneIds);

    @Cacheable("RepositoryCache")
    List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds, String projection,
                                                           Integer pageSize, Integer pageNumber,
                                                           String sortBy, String direction);

    @Cacheable("RepositoryCache")
    MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds,
                                                             List<Integer> entrezGeneIds);

    @Cacheable("RepositoryCache")
    List<Mutation> fetchMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                    List<Integer> entrezGeneIds, Boolean snpOnly, String projection,
                                                    Integer pageSize, Integer pageNumber, String sortBy,
                                                    String direction);

    @Cacheable("RepositoryCache")
    MutationMeta fetchMetaMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                      List<Integer> entrezGeneIds);

    @Cacheable("RepositoryCache")
    List<MutationCountByGene> getSampleCountByEntrezGeneIdsAndSampleIds(String molecularProfileId,
                                                                        List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds);

    @Cacheable("RepositoryCache")
    List<MutationCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                        List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds);

    @Cacheable("RepositoryCache")
    List<MutationCountByGene> getPatientCountByEntrezGeneIdsAndSampleIds(String molecularProfileId,
                                                                         List<String> patientIds,
                                                                         List<Integer> entrezGeneIds);

    @Cacheable("RepositoryCache")
    MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart, 
                                                       Integer proteinPosEnd);
}
