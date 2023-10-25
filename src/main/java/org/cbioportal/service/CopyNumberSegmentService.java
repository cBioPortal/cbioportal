package org.cbioportal.service;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface CopyNumberSegmentService {

    List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId, String chromosome, String projection,
                                                             Integer pageSize, Integer pageNumber, String sortBy,
                                                             String direction) 
        throws SampleNotFoundException, StudyNotFoundException;

    BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId, String chromosome) 
        throws SampleNotFoundException, StudyNotFoundException;

    List<CopyNumberSeg> fetchCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String chromosome, String projection);

    BaseMeta fetchMetaCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String chromosome);

    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String studyId, String sampleListId, String chromosome, String projection);
}
