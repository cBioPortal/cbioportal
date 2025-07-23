package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

public class VirtualisationUtils {
  public static String calculateUniqueKey(String virtualStudyId, String uniqueKey) {
    if (uniqueKey == null || uniqueKey.isEmpty()) {
      return uniqueKey;
    }
    return virtualStudyId + "_" + uniqueKey;
  }

  public static String calculateOriginalMolecularProfileId(
      String molecularProfileId, String virtualStudyId, String originalStudyId) {
    return molecularProfileId.replace(virtualStudyId + "_", originalStudyId + "_");
  }

  public static String calculateVirtualMolecularProfileId(
      String molecularProfileId, String virtualStudyId, String originalStudyId) {
    return molecularProfileId.replace(originalStudyId + "_", virtualStudyId + "_");
  }

  /**
   * Converts two lists of study IDs and sample IDs into a list of StudySamplePair objects.
   *
   * @param studyIds the list of study IDs
   * @param sampleIds the list of sample IDs
   * @return a list of StudySamplePair objects
   * @throws IllegalArgumentException if the sizes of the two lists do not match
   */
  public static List<StudyScopedId> toStudySamplePairs(
      List<String> studyIds, List<String> sampleIds) {
    if (studyIds.size() != sampleIds.size()) {
      throw new IllegalArgumentException(
          "The number of study IDs and sample IDs must be the same.");
    }
    List<StudyScopedId> studyScopedIds = new ArrayList<>();
    for (int i = 0; i < sampleIds.size(); i++) {
      studyScopedIds.add(new StudyScopedId(studyIds.get(i), sampleIds.get(i)));
    }
    return studyScopedIds;
  }

  /**
   * Converts a list of StudySamplePair objects into a pair of lists: one for study IDs and one for
   * sample IDs.
   *
   * @param studySamplePairs the list of StudySamplePair objects
   * @return a pair of lists containing study IDs and sample IDs
   */
  public static Pair<List<String>, List<String>> toStudyAndSampleIdLists(
      Iterable<StudyScopedId> studySamplePairs) {
    List<String> studyIds = new ArrayList<>();
    List<String> sampleIds = new ArrayList<>();
    for (StudyScopedId pair : studySamplePairs) {
      studyIds.add(pair.getStudyStableId());
      sampleIds.add(pair.getStableId());
    }
    return ImmutablePair.of(studyIds, sampleIds);
  }

  public static void checkSingleSourceStudy(VirtualStudy virtualStudy) {
    if (virtualStudy.getData().getStudies().size() != 1) {
      throw new IllegalStateException(
          "Virtual study should have exactly one study, but found "
              + virtualStudy.getData().getStudies().size());
    }
  }
}
