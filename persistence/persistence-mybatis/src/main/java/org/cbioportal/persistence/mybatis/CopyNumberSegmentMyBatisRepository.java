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
    public List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId,
                                                                    String projection, Integer pageSize,
                                                                    Integer pageNumber, String sortBy,
                                                                    String direction) {

        return copyNumberSegmentMapper.getCopyNumberSegments(Arrays.asList(studyId), Arrays.asList(sampleId), 
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId) {

        return copyNumberSegmentMapper.getMetaCopyNumberSegments(Arrays.asList(studyId), Arrays.asList(sampleId));
    }

    @Override
    public List<Integer> fetchSamplesWithCopyNumberSegments(List<String> studyIds, List<String> sampleIds) {
        return copyNumberSegmentMapper.getSamplesWithCopyNumberSegments(studyIds, sampleIds);
    }
    
    @Override
    public List<CopyNumberSeg> fetchCopyNumberSegments(List<String> studyIds, List<String> sampleIds, 
                                                       String projection) {
        
        return copyNumberSegmentMapper.getCopyNumberSegments(studyIds, sampleIds, projection, 0, 0, null, null);
    }

    @Override
    public BaseMeta fetchMetaCopyNumberSegments(List<String> studyIds, List<String> sampleIds) {
        
        return copyNumberSegmentMapper.getMetaCopyNumberSegments(studyIds, sampleIds);
    }

    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String studyId, String sampleListId, 
                                                                   String projection) {
        
        return copyNumberSegmentMapper.getCopyNumberSegmentsBySampleListId(studyId, sampleListId, projection);
    }
}
