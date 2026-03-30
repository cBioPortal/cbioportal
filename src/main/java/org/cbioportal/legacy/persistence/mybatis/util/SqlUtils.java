package org.cbioportal.legacy.persistence.mybatis.util;

import java.util.ArrayList;
import java.util.List;

/** Utility class for SQL operations in MyBatis mappers */
public class SqlUtils {

  private static final int DEFAULT_BATCH_SIZE = 5000;

  /**
   * Partitions a list into sublists of at most {@code batchSize} elements. Used to split large IN
   * clauses into batched OR groups to stay within StarRocks' expr_children_limit.
   *
   * @param list the list to partition
   * @param batchSize maximum elements per partition
   * @return list of sublists
   */
  public static <T> List<List<T>> partitionList(List<T> list, int batchSize) {
    if (list == null) {
      return List.of();
    }
    List<List<T>> partitions = new ArrayList<>();
    for (int i = 0; i < list.size(); i += batchSize) {
      partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
    }
    return partitions;
  }

  /**
   * Partitions an array into sublists of at most {@link #DEFAULT_BATCH_SIZE} elements.
   *
   * @param array the array to partition
   * @return list of sublists
   */
  public static List<List<String>> partitionArray(String[] array) {
    if (array == null) {
      return List.of();
    }
    return partitionList(List.of(array), DEFAULT_BATCH_SIZE);
  }

  /**
   * Combines study IDs and patient/sample IDs into unique keys for efficient array parameter usage.
   * This helps optimize ClickHouse JDBC performance by reducing the number of prepared statement
   * parameters.
   *
   * @param studyIds List of study identifiers
   * @param entityIds List of patient or sample identifiers (corresponding to studyIds by index)
   * @return Array of combined unique keys in format "studyId:entityId"
   */
  public static String[] combineStudyAndEntityIds(List<String> studyIds, List<String> entityIds) {
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

  /**
   * Converts a List of strings to a String array for use with ArrayTypeHandler. ArrayTypeHandler
   * requires Java arrays, not ArrayList objects.
   *
   * @param list List of strings to convert
   * @return Array of strings
   */
  public static String[] listToArray(List<String> list) {
    if (list == null) {
      return null;
    }
    return list.toArray(new String[0]);
  }
}
