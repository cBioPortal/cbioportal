package org.cbioportal.persistence;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface StudyRepository {

    @Cacheable("GeneralRepositoryCache")
    List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber,
                                    String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaStudies(String keyword);

    @Cacheable("GeneralRepositoryCache")
    CancerStudy getStudy(String studyId, String projection);

    @Cacheable("GeneralRepositoryCache")
    List<CancerStudy> fetchStudies(List<String> studyIds, String projection);
    
    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaStudies(List<String> studyIds);
    
    @Cacheable("GeneralRepositoryCache")
    CancerStudyTags getTags(String studyId);

    @Cacheable("GeneralRepositoryCache")
    List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds);
}
