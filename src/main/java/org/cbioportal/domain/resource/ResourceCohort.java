package org.cbioportal.domain.resource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.cbioportal.legacy.web.parameter.PatientIdentifier;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

/**
 * Turns (studyId, stableId) identifier pairs into the two array forms the resource SQL needs.
 *
 * <p>{@code resource_data} stores <em>stable</em> ids, which are unique only within a study, so
 * matching a cohort on bare ids alone selects the cross product of the requested studies and the
 * requested ids rather than the cohort itself. With two studies that each contain a sample called
 * {@code TCGA-A1-A0SB-01}, a cohort holding only the first study's copy also matches the second
 * study's rows, and the distinct counts collapse the two samples into one.
 *
 * <p>Both forms are therefore needed. The bare ids are what the {@code resource_data} sorting key
 * can prune on, so they stay in the query as the cheap predicate; the study-qualified tuples are
 * what make the match correct. The tuple form matches {@code SampleDataFilterUtil}, which applies
 * the same {@code <studyId>_<stableId>} composition for study-view sample filtering.
 */
final class ResourceCohort {

  private ResourceCohort() {}

  static String[] sampleIds(List<SampleIdentifier> identifiers) {
    return distinct(identifiers, SampleIdentifier::getSampleId);
  }

  static String[] sampleTuples(List<SampleIdentifier> identifiers) {
    return distinct(identifiers, id -> tuple(id.getStudyId(), id.getSampleId()));
  }

  static String[] patientIds(List<PatientIdentifier> identifiers) {
    return distinct(identifiers, PatientIdentifier::getPatientId);
  }

  static String[] patientTuples(List<PatientIdentifier> identifiers) {
    return distinct(identifiers, id -> tuple(id.getStudyId(), id.getPatientId()));
  }

  /**
   * Whether the study-qualified predicate is needed at all. With a single study a stable id cannot
   * be ambiguous, so the tuple condition is skipped -- it would only add a {@code concat} per
   * surviving row. This mirrors the guard in {@code ClickhouseSampleMapper}.
   */
  static boolean spansMultipleStudies(List<String> studyIds) {
    return studyIds != null && new LinkedHashSet<>(studyIds).size() > 1;
  }

  private static String tuple(String studyId, String stableId) {
    return studyId + "_" + stableId;
  }

  private static <T> String[] distinct(List<T> identifiers, Function<T, String> extractor) {
    if (identifiers == null || identifiers.isEmpty()) {
      return new String[0];
    }
    Set<String> values = new LinkedHashSet<>();
    for (T identifier : identifiers) {
      if (identifier == null) {
        continue;
      }
      String value = extractor.apply(identifier);
      if (value != null) {
        values.add(value);
      }
    }
    return values.toArray(new String[0]);
  }
}
