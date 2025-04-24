package org.cbioportal.application.file.export.mappers;

import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.CnaSegment;

public interface CnaSegmentMapper {
    Cursor<CnaSegment> getCnaSegments(String studyId);

    boolean hasCnaSegments(String studyId);
}
