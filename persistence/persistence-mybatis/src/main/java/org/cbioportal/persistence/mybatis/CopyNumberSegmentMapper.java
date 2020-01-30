package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;

public interface CopyNumberSegmentMapper {
    List<CopyNumberSeg> getCopyNumberSegments(
        List<String> studyIds,
        List<String> sampleIds,
        String chromosome,
        String projection,
        Integer limit,
        Integer offset,
        String sortBy,
        String direction
    );

    List<Integer> getSamplesWithCopyNumberSegments(
        List<String> studyIds,
        List<String> sampleIds,
        String chromosome
    );

    BaseMeta getMetaCopyNumberSegments(
        List<String> studyIds,
        List<String> sampleIds,
        String chromosome
    );

    List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(
        String studyId,
        String sampleListId,
        String chromosome,
        String projection
    );
}
