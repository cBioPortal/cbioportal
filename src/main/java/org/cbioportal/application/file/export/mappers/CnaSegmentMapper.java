package org.cbioportal.application.file.export.mappers;

import java.util.Set;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.CnaSegment;

public interface CnaSegmentMapper {
  Cursor<CnaSegment> getCnaSegments(String studyId, Set<String> sampleIds);

  boolean hasCnaSegments(String studyId, Set<String> sampleIds);
}
