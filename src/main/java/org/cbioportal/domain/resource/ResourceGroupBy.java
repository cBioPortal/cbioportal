package org.cbioportal.domain.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

/**
 * What a grouped resource table has one entry per.
 *
 * <p>{@link #PATIENT} groups every row that carries a patient id, so a patient-level row counts
 * with its patient alongside that patient's sample-level rows. {@link #SAMPLE} groups sample-level
 * rows only, since a patient-level row belongs to no sample. Study-level rows carry neither and
 * belong to no group.
 */
public enum ResourceGroupBy {
  PATIENT,
  SAMPLE;

  /**
   * Parses the request value, ignoring case. Null means no grouping; anything other than the two
   * modes is a bad request. The request body is parsed through this once, so the rest of the
   * request reads the mode rather than the text.
   */
  @JsonCreator
  public static ResourceGroupBy parse(String value) {
    if (value == null) {
      return null;
    }
    return switch (value.toLowerCase(Locale.ROOT)) {
      case "patient" -> PATIENT;
      case "sample" -> SAMPLE;
      default ->
          throw new InvalidResourceTableQueryException(
              "groupBy must be 'patient' or 'sample', not '" + value + "'");
    };
  }

  /** The request value: {@code patient} or {@code sample}. */
  @JsonValue
  public String value() {
    return name().toLowerCase(Locale.ROOT);
  }
}
