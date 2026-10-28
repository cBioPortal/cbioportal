package org.cbioportal.domain.ascn.util;

import java.util.List;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

public abstract class AscnSegmentUtil {
  private AscnSegmentUtil() {}

  /** Extracts the study IDs, parallel to {@link #extractSampleIds}. */
  public static List<String> extractStudyIds(List<SampleIdentifier> identifiers) {
    return identifiers.stream().map(SampleIdentifier::getStudyId).toList();
  }

  /** Extracts the sample IDs, parallel to {@link #extractStudyIds}. */
  public static List<String> extractSampleIds(List<SampleIdentifier> identifiers) {
    return identifiers.stream().map(SampleIdentifier::getSampleId).toList();
  }
}
