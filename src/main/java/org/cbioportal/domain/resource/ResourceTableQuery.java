package org.cbioportal.domain.resource;

import java.util.List;
import org.cbioportal.legacy.web.parameter.PatientIdentifier;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

public record ResourceTableQuery(
    List<String> studyIds,
    String resourceId,
    List<PatientIdentifier> patientIdentifiers,
    List<SampleIdentifier> sampleIdentifiers,
    String search,
    int pageNumber,
    int pageSize,
    String sortBy,
    String direction,
    List<ResourceColumnFilter> filters) {

  /**
   * The id lists as ClickHouse native arrays, bound as a single JDBC array parameter via MyBatis'
   * {@code ArrayTypeHandler} rather than expanded into one placeholder per id by a {@code
   * <foreach>}. Expanding a cohort into one placeholder per id makes the SQL and the bound
   * parameter count grow with the cohort, on every statement the request issues. See
   * cBioPortal/cbioportal#11296, which established this pattern for study-view sample filtering.
   *
   * <p>The cohort arrives as (studyId, stableId) pairs and is projected into two forms: the bare
   * stable ids, which the sorting key can prune on, and the study-qualified tuples, which make the
   * match correct across studies. See {@link ResourceCohort}.
   *
   * <p>Accessors are record-style on purpose: for records MyBatis' Reflector registers properties
   * under the raw method name, so a bean-style getter would be exposed as {@code getStudyIdsArray}.
   */
  public String[] studyIdsArray() {
    return studyIds == null ? new String[0] : studyIds.toArray(new String[0]);
  }

  public String[] patientIdsArray() {
    return ResourceCohort.patientIds(patientIdentifiers);
  }

  public String[] sampleIdsArray() {
    return ResourceCohort.sampleIds(sampleIdentifiers);
  }

  public String[] patientTuplesArray() {
    return ResourceCohort.patientTuples(patientIdentifiers);
  }

  public String[] sampleTuplesArray() {
    return ResourceCohort.sampleTuples(sampleIdentifiers);
  }

  /** Whether the study-qualified tuple predicate is needed; see {@link ResourceCohort}. */
  public boolean multiStudy() {
    return ResourceCohort.spansMultipleStudies(studyIds);
  }

  public boolean hasPatients() {
    return patientIdentifiers != null && !patientIdentifiers.isEmpty();
  }

  public boolean hasSamples() {
    return sampleIdentifiers != null && !sampleIdentifiers.isEmpty();
  }

  public int offset() {
    return Math.max(pageNumber, 0) * limit();
  }

  public int limit() {
    return Math.max(pageSize, 0);
  }
}
