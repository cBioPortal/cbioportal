package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface CopyNumberSegmentMapper {
    
    List<CopyNumberSeg> getCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String projection, 
                                              Integer limit, Integer offset, String sortBy, String direction);
    
    List<Integer> getSamplesWithCopyNumberSegments(List<String> studyIds, List<String> sampleIds);

    BaseMeta getMetaCopyNumberSegments(List<String> studyIds, List<String> sampleIds);
    
    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String studyId, String sampleListId, String projection);
}
