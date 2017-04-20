package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class DiscreteCopyNumberMyBatisRepository implements DiscreteCopyNumberRepository {

    @Autowired
    private DiscreteCopyNumberMapper discreteCopyNumberMapper;

    @Override
    public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId, 
                                                                                             String sampleListId, 
                                                                                             List<Integer> alterations, 
                                                                                             String projection) {

        return discreteCopyNumberMapper.getDiscreteCopyNumbersBySampleListId(geneticProfileId, sampleListId, null, 
            alterations, projection);
    }

    @Override
    public BaseMeta getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(String geneticProfileId, 
                                                                             String sampleListId, 
                                                                             List<Integer> alterations) {

        return discreteCopyNumberMapper.getMetaDiscreteCopyNumbersBySampleListId(geneticProfileId, sampleListId, null, 
            alterations);
    }

    @Override
    public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInGeneticProfile(String geneticProfileId,
                                                                                 List<String> sampleIds,
                                                                                 List<Integer> entrezGeneIds,
                                                                                 List<Integer> alterations,
                                                                                 String projection) {

        return discreteCopyNumberMapper.getDiscreteCopyNumbersBySampleIds(geneticProfileId, sampleIds, entrezGeneIds, 
            alterations, projection);
    }

    @Override
    public BaseMeta fetchMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                                 List<Integer> entrezGeneIds, 
                                                                 List<Integer> alterations) {

        return discreteCopyNumberMapper.getMetaDiscreteCopyNumbersBySampleIds(geneticProfileId, sampleIds, 
            entrezGeneIds, alterations);
    }

    @Override
    public List<CopyNumberSampleCountByGene> getSampleCountByGeneAndAlteration(String geneticProfileId,
                                                                               List<Integer> entrezGeneIds,
                                                                               List<Integer> alterations) {

        return discreteCopyNumberMapper.getSampleCountByGeneAndAlteration(geneticProfileId, entrezGeneIds, alterations);
    }
}
