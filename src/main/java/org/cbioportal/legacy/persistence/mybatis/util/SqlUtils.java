package org.cbioportal.legacy.persistence.mybatis.util;

import java.util.List;

/** Utility class for SQL operations in MyBatis mappers */
public class SqlUtils {

  /**
   * Combines study IDs and patient/sample IDs into unique keys for efficient array parameter usage.
   * This helps optimize ClickHouse JDBC performance by reducing the number of prepared statement
   * parameters.
   *
   * @param studyIds List of study identifiers
   * @param entityIds List of patient or sample identifiers (corresponding to studyIds by index)
   * @return Array of combined unique keys in format "studyId:entityId"
   */
  public static String[] combineStudyAndPatientIds(List<String> studyIds, List<String> entityIds) {
    if (studyIds == null || entityIds == null || studyIds.size() != entityIds.size()) {
      throw new IllegalArgumentException(
          "studyIds and entityIds must be non-null and have the same size");
    }

    String[] combinedKeys = new String[studyIds.size()];
    for (int i = 0; i < studyIds.size(); i++) {
      combinedKeys[i] = studyIds.get(i) + ":" + entityIds.get(i);
    }

    return combinedKeys;
  }
}
