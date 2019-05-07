package org.cbioportal.persistence;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface StudyRepository {

    @Cacheable("RepositoryCache")
    List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber,
                                    String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaStudies(String keyword);

    @Cacheable("RepositoryCache")
    CancerStudy getStudy(String studyId, String projection);

    @Cacheable("RepositoryCache")
    List<CancerStudy> fetchStudies(List<String> studyIds, String projection);
    
    @Cacheable("RepositoryCache")
    BaseMeta fetchMetaStudies(List<String> studyIds);
    
    @Cacheable("RepositoryCache")
    CancerStudyTags getTags(String studyId);

    @Cacheable("RepositoryCache")
    List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds);
}
