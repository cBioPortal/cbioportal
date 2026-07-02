package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.apache.ibatis.session.ResultHandler;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.cbioportal.legacy.model.meta.BaseMeta;

public interface CopyNumberSegmentMapper {

  List<CopyNumberSeg> getCopyNumberSegments(
      List<String> studyIds,
      List<String> sampleIds,
      String chromosome,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  /**
   * Streams copy number segments for the given studies/samples to {@code handler} one row at a time
   * (ordered by seg_id), so the full result set is never materialized in memory. Mirrors the
   * unpaged "fetch" use of {@link #getCopyNumberSegments} but for large multi-study result sets.
   */
  void streamCopyNumberSegments(
      List<String> studyIds,
      List<String> sampleIds,
      String chromosome,
      String projection,
      ResultHandler<CopyNumberSeg> handler);

  List<Integer> getSamplesWithCopyNumberSegments(
      List<String> studyIds, List<String> sampleIds, String chromosome);

  BaseMeta getMetaCopyNumberSegments(
      List<String> studyIds, List<String> sampleIds, String chromosome);

  List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(
      String studyId, String sampleListId, String chromosome, String projection);
}
