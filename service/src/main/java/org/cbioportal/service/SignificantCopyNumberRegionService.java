package org.cbioportal.service;

import org.cbioportal.model.Gistic;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SignificantCopyNumberRegionService {
    
    List<Gistic> getSignificantCopyNumberRegions(String studyId, String projection, Integer pageSize, 
                                                 Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaSignificantCopyNumberRegions(String studyId);
}
