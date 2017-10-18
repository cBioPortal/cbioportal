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
    public List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String molecularProfileId, String sampleId,
                                                                    String projection, Integer pageSize,
                                                                    Integer pageNumber, String sortBy,
                                                                    String direction) {

        List<CopyNumberSeg> result = copyNumberSegmentMapper.getCopyNumberSegments(Arrays.asList(molecularProfileId), Arrays.asList(sampleId), 
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        return result;
    }

    @Override
    public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String molecularProfileId, String sampleId) {

        return copyNumberSegmentMapper.getMetaCopyNumberSegments(Arrays.asList(molecularProfileId), Arrays.asList(sampleId));
    }

    @Override
    public List<CopyNumberSeg> fetchCopyNumberSegments(List<String> molecularProfileIds, List<String> sampleIds, 
                                                       String projection) {
        
        return copyNumberSegmentMapper.getCopyNumberSegments(molecularProfileIds, sampleIds, projection, 0, 0, null, null);
    }

    @Override
    public BaseMeta fetchMetaCopyNumberSegments(List<String> molecularProfileIds, List<String> sampleIds) {
        
        return copyNumberSegmentMapper.getMetaCopyNumberSegments(molecularProfileIds, sampleIds);
    }

    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String molecularProfileId, String sampleListId, 
                                                                   String projection) {
        
        return copyNumberSegmentMapper.getCopyNumberSegmentsBySampleListId(molecularProfileId, sampleListId, projection);
    }
}
