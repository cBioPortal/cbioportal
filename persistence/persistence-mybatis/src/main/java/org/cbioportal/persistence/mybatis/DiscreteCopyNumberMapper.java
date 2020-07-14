package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface DiscreteCopyNumberMapper {

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersBySampleListId(String molecularProfileId, String sampleListId,
                                                                      List<Integer> entrezGeneIds,
                                                                      List<Integer> alterationTypes, String projection);

    BaseMeta getMetaDiscreteCopyNumbersBySampleListId(String molecularProfileId, String sampleListId,
                                                      List<Integer> entrezGeneIds, List<Integer> alterationTypes);

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersBySampleIds(String molecularProfileId, List<String> sampleIds,
                                                                   List<Integer> entrezGeneIds,
                                                                   List<Integer> alterationTypes, String projection);

    List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                                   List<String> sampleIds,
                                                                                   List<Integer> entrezGeneIds,
                                                                                   List<Integer> alterationTypes, 
                                                                                   String projection);

    BaseMeta getMetaDiscreteCopyNumbersBySampleIds(String molecularProfileId, List<String> sampleIds,
                                                   List<Integer> entrezGeneIds, List<Integer> alterationTypes);

    List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String molecularProfileId,
                                                                              List<String> sampleIds,
                                                                              List<Integer> entrezGeneIds,
                                                                              List<Integer> alterations);

}
