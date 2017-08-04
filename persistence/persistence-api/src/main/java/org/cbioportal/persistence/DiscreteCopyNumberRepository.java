package org.cbioportal.persistence;

import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface DiscreteCopyNumberRepository {

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId,
                                                                                      String sampleListId,
                                                                                      List<Integer> entrezGeneIds,
                                                                                      List<Integer> alterationTypes,
                                                                                      String projection);


    BaseMeta getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId,
                                                                      List<Integer> entrezGeneIds,
                                                                      List<Integer> alterationTypes);

    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInGeneticProfile(String geneticProfileId,
                                                                          List<String> sampleIds,
                                                                          List<Integer> entrezGeneIds,
                                                                          List<Integer> alterationTypes,
                                                                          String projection);

    BaseMeta fetchMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                          List<Integer> entrezGeneIds, List<Integer> alterationTypes);

    List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String geneticProfileId,
                                                                              List<String> sampleIds,
                                                                              List<Integer> entrezGeneIds,
                                                                              List<Integer> alterations);

    List<CopyNumberCountByGene> getPatientCountByGeneAndAlterationAndPatientIds(String geneticProfileId,
                                                                                List<String> patientIds,
                                                                                List<Integer> entrezGeneIds,
                                                                                List<Integer> alterations);
}
