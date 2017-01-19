package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

        return copyNumberSegmentMapper.getCopyNumberSegments(studyId, sampleId, projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId) {

        return copyNumberSegmentMapper.getMetaCopyNumberSegments(studyId, sampleId);
    }
}
