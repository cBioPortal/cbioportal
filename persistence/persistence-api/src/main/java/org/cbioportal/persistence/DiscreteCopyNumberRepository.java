package org.cbioportal.persistence;

import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface DiscreteCopyNumberRepository {

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId, 
                                                                                      String sampleListId, 
                                                                                      List<Integer> alterations, 
                                                                                      String projection);


    BaseMeta getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId, 
                                                                      List<Integer> alterations);

    List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInGeneticProfile(String geneticProfileId,
                                                                          List<String> sampleIds,
                                                                          List<Integer> entrezGeneIds,
                                                                          List<Integer> alterations, String projection);

    BaseMeta fetchMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                          List<Integer> entrezGeneIds, List<Integer> alterations);

    List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlteration(String geneticProfileId,
                                                                        List<Integer> entrezGeneIds,
                                                                        List<Integer> alterations);
}
