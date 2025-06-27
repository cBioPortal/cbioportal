package org.cbioportal.application.file.export.repositories.mybatis;

import java.util.Set;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.MafRecord;

/**
 * Mapper interface for retrieving MAF (Mutation Annotation Format) records. This interface provides
 * methods to access MAF records for specific molecular profiles and sample IDs.
 */
public interface MafRecordMapper {
  /**
   * Retrieves MAF records for a specific molecular profile and set of sample IDs.
   *
   * @param molecularProfileStableId the stable ID of the molecular profile
   * @param sampleIds a set of sample IDs to check for MAF records; null set means all samples
   * @return cursor containing MAF records for the specified molecular profile and samples
   */
  Cursor<MafRecord> getMafRecords(String molecularProfileStableId, Set<String> sampleIds);
}
