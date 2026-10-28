package org.cbioportal.domain.ascn.repository;

import java.util.List;
import org.cbioportal.domain.ascn.AscnSegment;
import org.cbioportal.legacy.model.meta.BaseMeta;

/** Repository for allele-specific copy number segments. */
public interface AscnSegmentRepository {

  /**
   * @param studyIds List of studyIds
   * @param sampleIds List of sampleIds
   * @param limit page size, null or 0 for no limit
   */
  List<AscnSegment> getAscnSegments(
      List<String> studyIds, List<String> sampleIds, Integer limit, Integer offset);

  BaseMeta getMetaAscnSegments(List<String> studyIds, List<String> sampleIds);
}
