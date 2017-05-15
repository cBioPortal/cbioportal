package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface DiscreteCopyNumberMapper {

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersBySampleListId(String geneticProfileId, String sampleListId,
                                                                      List<Integer> entrezGeneIds,
                                                                      List<Integer> alterationTypes, String projection);

    BaseMeta getMetaDiscreteCopyNumbersBySampleListId(String geneticProfileId, String sampleListId,
                                                      List<Integer> entrezGeneIds, List<Integer> alterationTypes);

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersBySampleIds(String geneticProfileId, List<String> sampleIds,
                                                                   List<Integer> entrezGeneIds,
                                                                   List<Integer> alterationTypes, String projection);

    BaseMeta getMetaDiscreteCopyNumbersBySampleIds(String geneticProfileId, List<String> sampleIds,
                                                   List<Integer> entrezGeneIds, List<Integer> alterationTypes);

    List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlterationAndSampleListId(
        String geneticProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterations);

    List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String geneticProfileId,
                                                                                    List<String> sampleIds,
                                                                                    List<Integer> entrezGeneIds,
                                                                                    List<Integer> alterations);
}
