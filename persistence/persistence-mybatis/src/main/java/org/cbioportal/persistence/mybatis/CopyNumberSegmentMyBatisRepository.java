package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class CopyNumberSegmentMyBatisRepository implements CopyNumberSegmentRepository {
    
    @Autowired
    private CopyNumberSegmentMapper copyNumberSegmentMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;


    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String profileId, String sampleId,
                                                                    String projection, Integer pageSize,
                                                                    Integer pageNumber, String sortBy,
                                                                    String direction) {

        return copyNumberSegmentMapper.getCopyNumberSegments(Arrays.asList(profileId), Arrays.asList(sampleId), 
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String profileyId, String sampleId) {

        return copyNumberSegmentMapper.getMetaCopyNumberSegments(Arrays.asList(profileyId), Arrays.asList(sampleId));
    }

    @Override
    public List<CopyNumberSeg> fetchCopyNumberSegments(List<String> profileIds, List<String> sampleIds, 
                                                       String projection) {
        
        return copyNumberSegmentMapper.getCopyNumberSegments(profileIds, sampleIds, projection, 0, 0, null, null);
    }

    @Override
    public BaseMeta fetchMetaCopyNumberSegments(List<String> profileIds, List<String> sampleIds) {
        
        return copyNumberSegmentMapper.getMetaCopyNumberSegments(profileIds, sampleIds);
    }

    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String profileId, String sampleListId, 
                                                                   String projection) {
        
        return copyNumberSegmentMapper.getCopyNumberSegmentsBySampleListId(profileId, sampleListId, projection);
    }
}
