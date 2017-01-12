package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface CopyNumberSegmentMapper {
    
    List<CopyNumberSeg> getCopyNumberSegments(String studyId, String sampleId, String projection, Integer limit,
                                              Integer offset, String sortBy, String direction);

    BaseMeta getMetaCopyNumberSegments(String studyId, String sampleId);
}
