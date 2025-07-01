package org.cbioportal.application.file.export.repositories.mybatis;

import java.util.Set;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.CnaSegment;

/**
 * Mapper interface for retrieving copy number alteration (CNA) segments. This interface provides
 * methods to access CNA segments for specific studies and samples.
 */
public interface CnaSegmentMapper {
  /**
   * Retrieves CNA segments for a specific study and set of sample IDs.
   *
   * @param studyId the identifier of the study
   * @param sampleIds a set of sample IDs to filter the CNA segments; null set means all samples
   * @return a cursor containing CNA segments for the specified study and samples
   */
  Cursor<CnaSegment> getCnaSegments(String studyId, Set<String> sampleIds);

  /**
   * Checks if CNA segments exist for the specified study and sample IDs.
   *
   * @param studyId the identifier of the study
   * @param sampleIds a set of sample IDs to check for CNA segments; null set means all samples
   * @return true if CNA segments exist for the specified study and sample IDs, false otherwise
   */
  boolean hasCnaSegments(String studyId, Set<String> sampleIds);
}
