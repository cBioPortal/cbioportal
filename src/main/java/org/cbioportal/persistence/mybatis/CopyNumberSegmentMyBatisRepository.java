package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class CopyNumberSegmentMyBatisRepository implements CopyNumberSegmentRepository {
    
    @Autowired
    private CopyNumberSegmentMapper copyNumberSegmentMapper;


    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId, String chromosome,
                                                                    String projection, Integer pageSize,
                                                                    Integer pageNumber, String sortBy,
                                                                    String direction) {

        return copyNumberSegmentMapper.getCopyNumberSegments(Arrays.asList(studyId), Arrays.asList(sampleId), chromosome,
            projection, pageSize, PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId, String chromosome) {

        return copyNumberSegmentMapper.getMetaCopyNumberSegments(Arrays.asList(studyId), Arrays.asList(sampleId), chromosome);
    }

    @Override
    public List<Integer> fetchSamplesWithCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String chromosome) {
        return copyNumberSegmentMapper.getSamplesWithCopyNumberSegments(studyIds, sampleIds, chromosome);
    }
    
    @Override
    public List<CopyNumberSeg> fetchCopyNumberSegments(List<String> studyIds, 
                                                       List<String> sampleIds,
                                                       String chromosome,
                                                       String projection) {
        
        return copyNumberSegmentMapper.getCopyNumberSegments(studyIds, sampleIds, chromosome, projection, 0, 0, null, null);
    }

    @Override
    public BaseMeta fetchMetaCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String chromosome) {
        
        return copyNumberSegmentMapper.getMetaCopyNumberSegments(studyIds, sampleIds, chromosome);
    }

    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String studyId, 
                                                                   String sampleListId,
                                                                   String chromosome,
                                                                   String projection) {
        
        return copyNumberSegmentMapper.getCopyNumberSegmentsBySampleListId(studyId, sampleListId, chromosome, projection);
    }
}
