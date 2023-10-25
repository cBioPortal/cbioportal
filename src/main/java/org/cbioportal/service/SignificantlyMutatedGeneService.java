package org.cbioportal.service;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface SignificantlyMutatedGeneService {
    
    List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                              String sortBy, String direction) throws StudyNotFoundException;

    BaseMeta getMetaSignificantlyMutatedGenes(String studyId) throws StudyNotFoundException;
}
