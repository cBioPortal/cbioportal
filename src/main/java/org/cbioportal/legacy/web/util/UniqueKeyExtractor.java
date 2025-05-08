package org.cbioportal.legacy.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.cbioportal.legacy.utils.Encoder;

public class UniqueKeyExtractor {

  private UniqueKeyExtractor() {}

  public static Collection<String> extractUniqueKeys(List<String> uniqueKeys) {
    Collection<String> studyIds = new ArrayList<>();
    extractUniqueKeys(uniqueKeys, studyIds, null);
    return studyIds;
  }

  public static void extractUniqueKeys(
      List<String> uniqueKeys, Collection<String> studyIdsToReturn) {
    extractUniqueKeys(uniqueKeys, studyIdsToReturn, null);
  }

  public static void extractUniqueKeys(
      List<String> uniqueKeys,
      Collection<String> studyIdsToReturn,
      Collection<String> patientOrSampleIdsToReturn) {
    for (String uniqueKey : uniqueKeys) {
      String uniqueId = Encoder.decodeBase64(uniqueKey);
      String[] patientOrSampleAndStudyId = uniqueId.split(Encoder.DELIMITER);
      if (patientOrSampleAndStudyId.length == 2) {
        if (patientOrSampleIdsToReturn != null) {
          patientOrSampleIdsToReturn.add(patientOrSampleAndStudyId[0]);
        }
        studyIdsToReturn.add(patientOrSampleAndStudyId[1]);
      }
    }
  }
}
