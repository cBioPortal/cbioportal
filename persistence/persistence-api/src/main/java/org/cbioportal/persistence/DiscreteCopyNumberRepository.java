package org.cbioportal.persistence;

import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface DiscreteCopyNumberRepository {

    @Cacheable("RepositoryCache")
    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId,
                                                                                        String sampleListId,
                                                                                        List<Integer> entrezGeneIds,
                                                                                        List<Integer> alterationTypes,
                                                                                        String projection);


    @Cacheable("RepositoryCache")
    BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                        List<Integer> entrezGeneIds,
                                                                        List<Integer> alterationTypes);

    @Cacheable("RepositoryCache")
    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(String molecularProfileId,
                                                                            List<String> sampleIds,
                                                                            List<Integer> entrezGeneIds,
                                                                            List<Integer> alterationTypes,
                                                                            String projection);

    @Cacheable("RepositoryCache")
    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                                   List<String> sampleIds,
                                                                                   List<Integer> entrezGeneIds,
                                                                                   List<Integer> alterationTypes, 
                                                                                   String projection);

    @Cacheable("RepositoryCache")
    BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                            List<Integer> entrezGeneIds, List<Integer> alterationTypes);

    @Cacheable("RepositoryCache")
    List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String molecularProfileId,
                                                                              List<String> sampleIds,
                                                                              List<Integer> entrezGeneIds,
                                                                              List<Integer> alterations);

    @Cacheable("RepositoryCache")
    List<CopyNumberCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                          List<String> sampleIds, 
                                                                          List<Integer> entrezGeneIds, 
                                                                          List<Integer> alterations);

    @Cacheable("RepositoryCache")
    List<CopyNumberCountByGene> getPatientCountByGeneAndAlterationAndPatientIds(String molecularProfileId,
                                                                                List<String> patientIds,
                                                                                List<Integer> entrezGeneIds,
                                                                                List<Integer> alterations);
}
