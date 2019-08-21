package org.cbioportal.persistence;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface StudyRepository {

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber,
                                    String sortBy, String direction);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaStudies(String keyword);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    CancerStudy getStudy(String studyId, String projection);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<CancerStudy> fetchStudies(List<String> studyIds, String projection);
    
    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta fetchMetaStudies(List<String> studyIds);
    
    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    CancerStudyTags getTags(String studyId);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds);
}
