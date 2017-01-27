package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class DiscreteCopyNumberMyBatisRepository implements DiscreteCopyNumberRepository {
    
    @Autowired
    private DiscreteCopyNumberMapper discreteCopyNumberMapper;
    
    @Override
    public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, String sampleId,
                                                                               List<Integer> alterations, 
                                                                               String projection) {
        
        return discreteCopyNumberMapper.getDiscreteCopyNumbers(geneticProfileId, 
            sampleId == null ? null : Arrays.asList(sampleId), alterations, projection);
    }

    @Override
    public BaseMeta getMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, String sampleId, 
                                                               List<Integer> alterations) {
        
        return discreteCopyNumberMapper.getMetaDiscreteCopyNumbers(geneticProfileId, 
            sampleId == null ? null : Arrays.asList(sampleId), alterations);
    }

    @Override
    public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, 
                                                                                 List<String> sampleIds, 
                                                                                 List<Integer> alterations, 
                                                                                 String projection) {
        
        return discreteCopyNumberMapper.getDiscreteCopyNumbers(geneticProfileId, sampleIds, alterations, projection);
    }

    @Override
    public BaseMeta fetchMetaDiscreteCopyNumbersInGeneticProfile(String geneticProfileId, List<String> sampleIds, 
                                                                 List<Integer> alterations) {
        
        return discreteCopyNumberMapper.getMetaDiscreteCopyNumbers(geneticProfileId, sampleIds, alterations);
    }
}
