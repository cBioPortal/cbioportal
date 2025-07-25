package org.cbioportal.shared.util;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.util.UniqueKeyExtractor;
import org.springframework.data.util.Pair;
import org.springframework.data.util.StreamUtils;

public abstract class SampleDataFilterUtil {
  private SampleDataFilterUtil() {}

  public static Pair<List<String>, List<String>> extractStudyAndSampleIds(
      SampleFilter sampleFilter) {
    List<String> studyIds = new ArrayList<>();
    List<String> sampleIds = new ArrayList<>();

    if (sampleFilter.getSampleIdentifiers() != null) {
      for (SampleIdentifier sampleIdentifier : sampleFilter.getSampleIdentifiers()) {
        studyIds.add(sampleIdentifier.getStudyId());
        sampleIds.add(sampleIdentifier.getSampleId());
      }
    } else {
      UniqueKeyExtractor.extractUniqueKeys(sampleFilter.getUniqueSampleKeys(), studyIds, sampleIds);
    }

    return Pair.of(studyIds, sampleIds);
  }

  public static String[] generateStudyAndSampleTuples(
      List<String> studyIds, List<String> sampleIds) {
    return StreamUtils.zip(
            studyIds.stream(),
            sampleIds.stream(),
            (studyId, sampleId) -> String.format("%s_%s", studyId, sampleId))
        .toList()
        .toArray(new String[sampleIds.size()]);
  }
}
