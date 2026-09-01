package org.cbioportal.domain.resource;

import java.util.List;

public record ResourceTabsRequest(
    List<String> studyIds, List<String> patientIds, List<String> sampleIds) {

  /**
   * The id lists as ClickHouse native arrays, bound as a single JDBC array parameter via MyBatis'
   * {@code ArrayTypeHandler} rather than expanded into one placeholder per id by a {@code
   * <foreach>}. A 1,000-patient cohort otherwise produced ~45KB of SQL and ~2,100 bound parameters
   * on *every* statement the request issues. See cBioPortal/cbioportal#11296, which established
   * this pattern for study-view sample filtering.
   *
   * <p>Accessors are record-style on purpose: for records MyBatis' Reflector registers properties
   * under the raw method name, so a bean-style getter would be exposed as {@code getStudyIdsArray}.
   */
  public String[] studyIdsArray() {
    return toArray(studyIds);
  }

  public String[] patientIdsArray() {
    return toArray(patientIds);
  }

  public String[] sampleIdsArray() {
    return toArray(sampleIds);
  }

  private static String[] toArray(List<String> values) {
    return values == null ? new String[0] : values.toArray(new String[0]);
  }
}
