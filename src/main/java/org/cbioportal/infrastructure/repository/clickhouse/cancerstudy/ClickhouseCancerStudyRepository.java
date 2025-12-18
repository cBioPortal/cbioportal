package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import java.util.List;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.domain.cancerstudy.ResourceCount;
import org.cbioportal.domain.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 * Repository implementation for accessing cancer study metadata from ClickHouse. This class
 * delegates database queries to {@link ClickhouseCancerStudyMapper}.
 */
@Repository
public class ClickhouseCancerStudyRepository implements CancerStudyRepository {

  private final ClickhouseCancerStudyMapper cancerStudyMapper;

  /**
   * Constructs a new {@code ClickhouseCancerStudyRepository} with the required mapper.
   *
   * @param cancerStudyMapper the mapper responsible for executing ClickHouse queries
   */
  public ClickhouseCancerStudyRepository(ClickhouseCancerStudyMapper cancerStudyMapper) {
    this.cancerStudyMapper = cancerStudyMapper;
  }

  /**
   * Retrieves detailed metadata for all cancer studies.
   *
   * @param sortAndSearchCriteria the criteria used for sorting and searching the cancer study
   *     metadata. This includes parameters such as sort direction, sort by field, and search
   *     keywords.
   * @return a list of {@link CancerStudyMetadata} containing detailed metadata for each study
   */
  @Override
  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public List<CancerStudyMetadata> getCancerStudiesMetadata(
      SortAndSearchCriteria sortAndSearchCriteria) {
    return cancerStudyMapper.getCancerStudiesMetadata(sortAndSearchCriteria, List.of());
  }

  /**
   * Retrieves a summarized version of cancer study metadata.
   *
   * @param sortAndSearchCriteria the criteria used for sorting and searching the cancer study
   *     metadata. This includes parameters such as sort direction, sort by field, and search
   *     keywords.
   * @return a list of {@link CancerStudyMetadata} containing summarized metadata for each study
   */
  @Override
  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public List<CancerStudyMetadata> getCancerStudiesMetadataSummary(
      SortAndSearchCriteria sortAndSearchCriteria) {
    return cancerStudyMapper.getCancerStudiesMetadataSummary(sortAndSearchCriteria, List.of());
  }

  /**
   * @param studyViewFilterContext
   * @return
   */
  @Override
  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public List<String> getFilteredStudyIds(StudyViewFilterContext studyViewFilterContext) {
    return cancerStudyMapper.getFilteredStudyIds(studyViewFilterContext);
  }

  @Override
  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public List<ResourceCount> getResourceCountsForAllStudies() {
    return cancerStudyMapper.getResourceCountsForAllStudies();
  }
}
