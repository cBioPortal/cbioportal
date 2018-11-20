package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface StudyMapper {

    List<CancerStudy> getStudies(List<String> studyIds, String keyword, String projection, Integer limit, Integer offset, String sortBy, 
        String direction);

    BaseMeta getMetaStudies(List<String> studyIds, String keyword);

    CancerStudy getStudy(String studyId, String projection);

    CancerStudyTags getTags(String studyId);

    List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds);
}
