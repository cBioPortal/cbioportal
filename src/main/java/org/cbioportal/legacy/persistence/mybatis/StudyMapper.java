package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.CancerStudyTags;
import org.cbioportal.legacy.model.ResourceCount;
import org.cbioportal.legacy.model.meta.BaseMeta;

public interface StudyMapper {

  List<CancerStudy> getStudies(
      List<String> studyIds,
      String keyword,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  BaseMeta getMetaStudies(List<String> studyIds, String keyword);

  CancerStudy getStudy(String studyId, String projection);

  CancerStudyTags getTags(String studyId);

  List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds);

  List<ResourceCount> getResourceCountsForAllStudies();

  List<ResourceCount> getResourceCounts(List<String> studyIds);

  /**
   * Returns one row per identifier (study id, molecular profile stable id, sample list stable id)
   * owned by a study whose status is not AVAILABLE, with keys {@code identifier} and {@code
   * studyId}.
   */
  List<Map<String, String>> getUnavailableStudyIdentifiers();
}
