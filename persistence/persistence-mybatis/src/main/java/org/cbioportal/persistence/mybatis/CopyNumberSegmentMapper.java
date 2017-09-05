package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface CopyNumberSegmentMapper {
    
    List<CopyNumberSeg> getCopyNumberSegments(List<String> geneticProfileIds, List<String> sampleIds, String projection, 
                                              Integer limit, Integer offset, String sortBy, String direction);

    BaseMeta getMetaCopyNumberSegments(List<String> geneticProfileIds, List<String> sampleIds);
    
    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String geneticProfileId, String sampleListId, String projection);
}
