package org.cbioportal.service;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface CopyNumberSegmentService {

    List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String studyId, String profileId, String sampleId, String projection,
                                                             Integer pageSize, Integer pageNumber, String sortBy,
                                                             String direction) throws SampleNotFoundException, StudyNotFoundException;

    BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String profileId, String sampleId) throws SampleNotFoundException, StudyNotFoundException;

    List<CopyNumberSeg> fetchCopyNumberSegments(List<String> profileIds, List<String> sampleIds, String projection);

    BaseMeta fetchMetaCopyNumberSegments(List<String> profileIds, List<String> sampleIds);

    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String profileId, String sampleListId, String projection);
}
