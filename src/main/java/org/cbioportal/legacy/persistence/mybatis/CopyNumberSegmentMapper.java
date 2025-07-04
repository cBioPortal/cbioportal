package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.cbioportal.legacy.model.StudyScopedId;
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

  List<StudyScopedId> getSamplesWithCopyNumberSegments(
      List<String> studyIds, List<String> sampleIds, String chromosome);

  BaseMeta getMetaCopyNumberSegments(
      List<String> studyIds, List<String> sampleIds, String chromosome);

  List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(
      String studyId, String sampleListId, String chromosome, String projection);
}
