package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSampleCountByGene;
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

    List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlteration(String geneticProfileId, 
                                                                        List<Integer> entrezGeneIds, 
                                                                        List<Integer> alterations);
}
