package org.cbioportal.service;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface CopyNumberSegmentService {

    List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String studyId, String molecularProfileId, String sampleId, String projection,
                                                             Integer pageSize, Integer pageNumber, String sortBy,
                                                             String direction) throws SampleNotFoundException, StudyNotFoundException;

    BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String molecularProfileId, String sampleId) throws SampleNotFoundException, StudyNotFoundException;

    List<CopyNumberSeg> fetchCopyNumberSegments(List<String> molecularProfileIds, List<String> sampleIds, String projection);

    BaseMeta fetchMetaCopyNumberSegments(List<String> molecularProfileIds, List<String> sampleIds);

    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String molecularProfileId, String sampleListId, String projection);
}
