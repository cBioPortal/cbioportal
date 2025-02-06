package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

import java.util.List;

public interface SignificantCopyNumberRegionService {
    
    List<Gistic> getSignificantCopyNumberRegions(String studyId, String projection, Integer pageSize, 
                                                 Integer pageNumber, String sortBy, String direction) throws StudyNotFoundException;

    BaseMeta getMetaSignificantCopyNumberRegions(String studyId) throws StudyNotFoundException;
}
