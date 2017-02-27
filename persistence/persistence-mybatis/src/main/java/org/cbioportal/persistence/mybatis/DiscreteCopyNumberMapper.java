package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface DiscreteCopyNumberMapper {
    
    List<DiscreteCopyNumberData> getDiscreteCopyNumbers(String geneticProfileId, List<String> sampleIds, 
                                                        List<Integer> entrezGeneIds, List<Integer> alterations, 
                                                        String projection);

    BaseMeta getMetaDiscreteCopyNumbers(String geneticProfileId, List<String> sampleIds, List<Integer> entrezGeneIds,
                                        List<Integer> alterations);

    List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlteration(String geneticProfileId, 
                                                                        List<Integer> entrezGeneIds, 
                                                                        List<Integer> alterations);
}
