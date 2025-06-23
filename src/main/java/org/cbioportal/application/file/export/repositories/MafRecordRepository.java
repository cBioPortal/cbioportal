package org.cbioportal.application.file.export.repositories;

import java.util.Set;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.utils.CloseableIterator;

/**
 * Repository interface for retrieving MAF (Mutation Annotation Format) records. This interface
 * provides methods to access MAF records for specific molecular profiles and sample IDs.
 */
public interface MafRecordRepository {
  /**
   * Retrieves MAF records for a specific molecular profile and set of sample IDs.
   *
   * @param molecularProfileStableId the stable ID of the molecular profile
   * @param sampleIds a set of sample IDs to check for MAF records; null set means all samples
   * @return iterator containing MAF records for the specified molecular profile and samples
   */
  CloseableIterator<MafRecord> getMafRecords(
      String molecularProfileStableId, Set<String> sampleIds);
}
