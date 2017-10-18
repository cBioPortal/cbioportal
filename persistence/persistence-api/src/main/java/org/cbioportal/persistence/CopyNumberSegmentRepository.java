package org.cbioportal.persistence;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface CopyNumberSegmentRepository {

    List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String geneticProfileId, String sampleId, String projection,
                                                             Integer pageSize, Integer pageNumber, String sortBy,
                                                             String direction);

    BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String geneticProfileId, String sampleId);

    List<CopyNumberSeg> fetchCopyNumberSegments(List<String> geneticProfileIds, List<String> sampleIds, String projection);

    BaseMeta fetchMetaCopyNumberSegments(List<String> geneticProfileIds, List<String> sampleIds);

    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String geneticProfileId, String sampleListId, String projection);
}
