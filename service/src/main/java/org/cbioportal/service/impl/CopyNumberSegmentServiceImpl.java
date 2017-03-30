package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.service.CopyNumberSegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CopyNumberSegmentServiceImpl implements CopyNumberSegmentService {

    @Autowired
    private CopyNumberSegmentRepository copyNumberSegmentRepository;

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId,
                                                                    String projection, Integer pageSize,
                                                                    Integer pageNumber, String sortBy,
                                                                    String direction) {

        return copyNumberSegmentRepository.getCopyNumberSegmentsInSampleInStudy(studyId, sampleId, projection, pageSize,
            pageNumber, sortBy, direction);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId) {
        
        return copyNumberSegmentRepository.getMetaCopyNumberSegmentsInSampleInStudy(studyId, sampleId);
    }

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
    public List<CopyNumberSeg> fetchCopyNumberSegments(List<String> studyIds, List<String> sampleIds, 
                                                       String projection) {
        
        return copyNumberSegmentRepository.fetchCopyNumberSegments(studyIds, sampleIds, projection);
    }

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
    public BaseMeta fetchMetaCopyNumberSegments(List<String> studyIds, List<String> sampleIds) {
        
        return copyNumberSegmentRepository.fetchMetaCopyNumberSegments(studyIds, sampleIds);
    }
}
