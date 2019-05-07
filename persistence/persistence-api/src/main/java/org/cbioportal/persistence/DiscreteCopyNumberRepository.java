package org.cbioportal.persistence;

import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface DiscreteCopyNumberRepository {

    @Cacheable("GeneralRepositoryCache")
    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId,
                                                                                        String sampleListId,
                                                                                        List<Integer> entrezGeneIds,
                                                                                        List<Integer> alterationTypes,
                                                                                        String projection);


    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                        List<Integer> entrezGeneIds,
                                                                        List<Integer> alterationTypes);

    @Cacheable("GeneralRepositoryCache")
    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(String molecularProfileId,
                                                                            List<String> sampleIds,
                                                                            List<Integer> entrezGeneIds,
                                                                            List<Integer> alterationTypes,
                                                                            String projection);

    @Cacheable("GeneralRepositoryCache")
    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                                   List<String> sampleIds,
                                                                                   List<Integer> entrezGeneIds,
                                                                                   List<Integer> alterationTypes, 
                                                                                   String projection);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                            List<Integer> entrezGeneIds, List<Integer> alterationTypes);

    @Cacheable("GeneralRepositoryCache")
    List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String molecularProfileId,
                                                                              List<String> sampleIds,
                                                                              List<Integer> entrezGeneIds,
                                                                              List<Integer> alterations);

    @Cacheable("GeneralRepositoryCache")
    List<CopyNumberCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                          List<String> sampleIds, 
                                                                          List<Integer> entrezGeneIds, 
                                                                          List<Integer> alterations);

    @Cacheable("GeneralRepositoryCache")
    List<CopyNumberCountByGene> getPatientCountByGeneAndAlterationAndPatientIds(String molecularProfileId,
                                                                                List<String> patientIds,
                                                                                List<Integer> entrezGeneIds,
                                                                                List<Integer> alterations);
}
