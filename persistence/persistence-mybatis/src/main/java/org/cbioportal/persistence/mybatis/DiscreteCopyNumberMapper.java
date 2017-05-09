package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.DiscreteCopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface DiscreteCopyNumberMapper {

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersBySampleListId(String geneticProfileId, String sampleListId,
                                                                      List<Integer> entrezGeneIds,
                                                                      List<Integer> alterations, String projection);

    BaseMeta getMetaDiscreteCopyNumbersBySampleListId(String geneticProfileId, String sampleListId,
                                                      List<Integer> entrezGeneIds, List<Integer> alterations);

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersBySampleIds(String geneticProfileId, List<String> sampleIds,
                                                                   List<Integer> entrezGeneIds,
                                                                   List<Integer> alterations, String projection);

    BaseMeta getMetaDiscreteCopyNumbersBySampleIds(String geneticProfileId, List<String> sampleIds,
                                                   List<Integer> entrezGeneIds, List<Integer> alterations);

    List<DiscreteCopyNumberSampleCountByGene> getSampleCountByGeneAndAlterationAndSampleListId(
        String geneticProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterations);

    List<DiscreteCopyNumberSampleCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String geneticProfileId,
                                                                                            List<String> sampleIds,
                                                                                            List<Integer> entrezGeneIds,
                                                                                            List<Integer> alterations);
}
