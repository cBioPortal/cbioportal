package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.CancerStudyTags;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface StudyService {

    List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber,
                                    String sortBy, String direction, Authentication authentication, AccessLevel accessLevel);

    BaseMeta getMetaStudies(String keyword);

    CancerStudy getStudy(String studyId) throws StudyNotFoundException;

	List<CancerStudy> fetchStudies(List<String> studyIds, String projection);

	BaseMeta fetchMetaStudies(List<String> studyIds);

    CancerStudyTags getTags(String studyId, AccessLevel accessLevel);
    
    List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds);
}
