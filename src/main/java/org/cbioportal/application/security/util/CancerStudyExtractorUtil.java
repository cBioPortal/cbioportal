package org.cbioportal.application.security.util;

import java.util.Collection;
import java.util.List;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.util.UniqueKeyExtractor;

public class CancerStudyExtractorUtil {

  private CancerStudyExtractorUtil() {}

  public static Collection<String> extractCancerStudyIdsFromSampleFilter(
      SampleFilter sampleFilter, CacheMapUtil cacheMapUtil) {
    Collection<String> studyIds;

    if (sampleFilter.getSampleListIds() != null) {
      studyIds =
          extractCancerStudyIdsFromSampleListIds(sampleFilter.getSampleListIds(), cacheMapUtil);
    } else if (sampleFilter.getSampleIdentifiers() != null) {
      studyIds = extractCancerStudyIdsFromSampleIdentifiers(sampleFilter.getSampleIdentifiers());
    } else {
      studyIds = UniqueKeyExtractor.extractUniqueKeys(sampleFilter.getUniqueSampleKeys());
    }

    return studyIds;
  }

  public static Collection<String> extractCancerStudyIdsFromSampleListIds(
      List<String> sampleListIds, CacheMapUtil cacheMapUtil) {
    return sampleListIds.stream()
        .map(
            sampleListId ->
                cacheMapUtil.getSampleListMap().get(sampleListId).getCancerStudyIdentifier())
        .distinct()
        .toList();
  }

  public static Collection<String> extractCancerStudyIdsFromSampleIdentifiers(
      Collection<SampleIdentifier> sampleIdentifiers) {
    return sampleIdentifiers.stream().map(SampleIdentifier::getStudyId).distinct().toList();
  }
}
