package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.MutSig;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

import java.util.List;

public interface SignificantlyMutatedGeneService {
    
    List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                              String sortBy, String direction) throws StudyNotFoundException;

    BaseMeta getMetaSignificantlyMutatedGenes(String studyId) throws StudyNotFoundException;
}
