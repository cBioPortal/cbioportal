package org.cbioportal.service.impl;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantlyMutatedGeneRepository;
import org.cbioportal.service.SignificantlyMutatedGeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignificantlyMutatedGeneServiceImpl implements SignificantlyMutatedGeneService {

    @Autowired
    private SignificantlyMutatedGeneRepository significantlyMutatedGeneRepository;

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize,
                                                     Integer pageNumber, String sortBy, String direction) {

        return significantlyMutatedGeneRepository.getSignificantlyMutatedGenes(studyId, projection, pageSize,
            pageNumber, sortBy, direction);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public BaseMeta getMetaSignificantlyMutatedGenes(String studyId) {
        
        return significantlyMutatedGeneRepository.getMetaSignificantlyMutatedGenes(studyId);
    }
}
