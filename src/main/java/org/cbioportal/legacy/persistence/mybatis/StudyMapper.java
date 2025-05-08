package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.CancerStudyTags;
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
}
