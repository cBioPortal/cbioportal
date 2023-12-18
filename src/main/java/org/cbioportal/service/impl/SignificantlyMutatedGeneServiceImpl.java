package org.cbioportal.service.impl;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantlyMutatedGeneRepository;
import org.cbioportal.service.SignificantlyMutatedGeneService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignificantlyMutatedGeneServiceImpl implements SignificantlyMutatedGeneService {

    @Autowired
    private SignificantlyMutatedGeneRepository significantlyMutatedGeneRepository;
    @Autowired
    private StudyService studyService;

    @Override
    public List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize,
                                                     Integer pageNumber, String sortBy, String direction) 
        throws StudyNotFoundException {
        
        studyService.getStudy(studyId);

        return significantlyMutatedGeneRepository.getSignificantlyMutatedGenes(studyId, projection, pageSize,
            pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSignificantlyMutatedGenes(String studyId) throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return significantlyMutatedGeneRepository.getMetaSignificantlyMutatedGenes(studyId);
    }
}
