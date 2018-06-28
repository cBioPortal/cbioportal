package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.service.CopyNumberSegmentService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CopyNumberSegmentServiceImpl implements CopyNumberSegmentService {

    @Autowired
    private CopyNumberSegmentRepository copyNumberSegmentRepository;
    @Autowired
    private SampleService sampleService;

    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId,
                                                                    String projection, Integer pageSize,
                                                                    Integer pageNumber, String sortBy,
                                                                    String direction) throws SampleNotFoundException, 
        StudyNotFoundException {
        
        sampleService.getSampleInStudy(studyId, sampleId);

        return copyNumberSegmentRepository.getCopyNumberSegmentsInSampleInStudy(studyId, sampleId, projection, pageSize,
            pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId)
        throws SampleNotFoundException, StudyNotFoundException {

        sampleService.getSampleInStudy(studyId, sampleId);
        
        return copyNumberSegmentRepository.getMetaCopyNumberSegmentsInSampleInStudy(studyId, sampleId);
    }

    @Override
    public List<CopyNumberSeg> fetchCopyNumberSegments(List<String> studyIds, List<String> sampleIds, 
                                                       String projection) {
        
        return copyNumberSegmentRepository.fetchCopyNumberSegments(studyIds, sampleIds, projection);
    }

    @Override
    public BaseMeta fetchMetaCopyNumberSegments(List<String> studyIds, List<String> sampleIds) {
        
        return copyNumberSegmentRepository.fetchMetaCopyNumberSegments(studyIds, sampleIds);
    }

    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String studyId, String sampleListId, 
                                                                   String projection) {
        
        return copyNumberSegmentRepository.getCopyNumberSegmentsBySampleListId(studyId, sampleListId, projection);
    }
}
