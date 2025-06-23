package org.cbioportal.application.file.export.mappers;

import java.util.List;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.GenericEntityProperty;
import org.cbioportal.application.file.model.GeneticProfileData;

/**
 * Mapper interface for retrieving genetic profile data. This interface provides methods to access
 * genetic profile data, sample stable IDs, and distinct generic entity meta property names for a
 * given molecular profile.
 */
public interface GeneticProfileDataMapper {
  /**
   * Retrieves genetic profile data for a specific molecular profile stable ID.
   *
   * @param molecularProfileStableId the stable ID of the molecular profile
   * @return a cursor containing genetic profile data for the specified molecular profile
   */
  List<String> getSampleStableIds(String molecularProfileStableId);

  /**
   * Retrieves genetic profile data for a specific molecular profile stable ID.
   *
   * @param molecularProfileStableId the stable ID of the molecular profile
   * @return a cursor containing genetic profile data for the specified molecular profile
   */
  Cursor<GeneticProfileData> getData(String molecularProfileStableId);

  /**
   * Retrieves distinct generic entity meta property names for a specific molecular profile stable
   * ID.
   *
   * @param molecularProfileStableId the stable ID of the molecular profile
   * @return a list of distinct generic entity meta property names for the specified molecular
   *     profile
   */
  List<String> getDistinctGenericEntityMetaPropertyNames(String molecularProfileStableId);

  /**
   * Retrieves generic entity meta properties for a specific molecular profile stable ID.
   *
   * @param molecularProfileStableId the stable ID of the molecular profile
   * @return a cursor containing generic entity meta properties for the specified molecular profile
   */
  Cursor<GenericEntityProperty> getGenericEntityMetaProperties(String molecularProfileStableId);
}
