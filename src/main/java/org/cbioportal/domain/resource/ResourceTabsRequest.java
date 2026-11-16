package org.cbioportal.domain.resource;

import java.util.List;
import org.cbioportal.legacy.web.parameter.PatientIdentifier;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

public record ResourceTabsRequest(
    List<String> studyIds,
    List<PatientIdentifier> patientIdentifiers,
    List<SampleIdentifier> sampleIdentifiers) {

  /**
   * See {@link ResourceTableQuery} for why these are bound as ClickHouse arrays, why the accessors
   * are record-style, and why the cohort is projected into both bare ids and study-qualified
   * tuples.
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
}
