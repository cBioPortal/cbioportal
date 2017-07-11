package org.cbioportal.persistence;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface CopyNumberSegmentRepository {

    List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String profileId, String sampleId, String projection,
                                                             Integer pageSize, Integer pageNumber, String sortBy,
                                                             String direction);

    BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String profileId, String sampleId);

    List<CopyNumberSeg> fetchCopyNumberSegments(List<String> profileIds, List<String> sampleIds, String projection);

    BaseMeta fetchMetaCopyNumberSegments(List<String> profileIds, List<String> sampleIds);

    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String profileId, String sampleListId, String projection);
}
