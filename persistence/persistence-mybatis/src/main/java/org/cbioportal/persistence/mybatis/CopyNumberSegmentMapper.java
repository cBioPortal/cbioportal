package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface CopyNumberSegmentMapper {
    
    List<CopyNumberSeg> getCopyNumberSegments(List<String> molecularProfileIds, List<String> sampleIds, String projection, 
                                              Integer limit, Integer offset, String sortBy, String direction);

    BaseMeta getMetaCopyNumberSegments(List<String> molecularProfileIds, List<String> sampleIds);
    
    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String molecularProfileId, String sampleListId, String projection);
}
