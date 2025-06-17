package org.cbioportal.application.file.export.mappers;

import java.util.Set;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.StructuralVariant;

/**
 * Mapper interface for retrieving structural variants. This interface provides methods to access
 * structural variants for specific molecular profiles and sample IDs.
 */
public interface SVMapper {

  /**
   * Retrieves structural variants for a specific molecular profile and set of sample IDs.
   *
   * @param molecularProfileStableId the stable ID of the molecular profile
   * @param sampleIds a set of sample IDs to filter the structural variants; null set means all
   *     samples
   * @return cursor containing structural variants for the specified molecular profile and samples
   */
  Cursor<StructuralVariant> getStructuralVariants(
      String molecularProfileStableId, Set<String> sampleIds);
}
