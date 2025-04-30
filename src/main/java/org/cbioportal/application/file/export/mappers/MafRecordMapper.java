package org.cbioportal.application.file.export.mappers;

import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.MafRecord;

import java.util.Set;

public interface MafRecordMapper {
    Cursor<MafRecord> getMafRecords(String molecularProfileStableId, Set<String> sampleIds);
}
