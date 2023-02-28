package org.cbioportal.service;

import org.cbioportal.model.Gistic;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface SignificantCopyNumberRegionService {
    
    List<Gistic> getSignificantCopyNumberRegions(String studyId, String projection, Integer pageSize, 
                                                 Integer pageNumber, String sortBy, String direction) throws StudyNotFoundException;

    BaseMeta getMetaSignificantCopyNumberRegions(String studyId) throws StudyNotFoundException;
}
