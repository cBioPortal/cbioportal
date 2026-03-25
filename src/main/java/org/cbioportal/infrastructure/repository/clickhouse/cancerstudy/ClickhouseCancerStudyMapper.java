package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.domain.cancerstudy.ResourceCount;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.shared.SortAndSearchCriteria;

/**
 * Provides methods for retrieving cancer study metadata from a ClickHouse database. This interface
 * defines the contract for fetching detailed and summarized metadata for cancer studies based on
 * specified criteria.
 */
public interface ClickhouseCancerStudyMapper {
  /**
   * Retrieves detailed metadata for cancer studies based on the provided sorting, search criteria,
   * and a list of study IDs. This method is intended to return comprehensive information about each
   * study.
   *
   * @param sortAndSearchCriteria the criteria used for sorting and searching the cancer study
   *     metadata. This includes parameters such as sort direction, sort by field, and search
   *     keywords.
   * @param studyIds a list of study IDs to filter the results. If empty, all studies matching the
   *     criteria should be returned.
   * @return a list of {@link CancerStudyMetadata} containing detailed metadata for each study that
   *     matches the provided criteria and study IDs. The list may be empty if no studies match the
   *     criteria.
   */
  List<CancerStudyMetadata> getCancerStudiesMetadata(
      SortAndSearchCriteria sortAndSearchCriteria, List<String> studyIds);

  /**
   * Retrieves a summarized version of cancer study metadata based on the provided sorting, search
   * criteria, and a list of study IDs. This method is intended to return a concise overview of each
   * study.
   *
   * @param sortAndSearchCriteria the criteria used for sorting and searching the cancer study
   *     metadata. This includes parameters such as sort direction, sort by field, and search
   *     keywords.
   * @param studyIds a list of study IDs to filter the results. If empty, all studies matching the
   *     criteria should be returned.
   * @return a list of {@link CancerStudyMetadata} containing summarized metadata for each study
   *     that matches the provided criteria and study IDs. The list may be empty if no studies match
   *     the criteria.
   */
  List<CancerStudyMetadata> getCancerStudiesMetadataSummary(
      SortAndSearchCriteria sortAndSearchCriteria, List<String> studyIds);

  List<String> getFilteredStudyIds(
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  List<ResourceCount> getResourceCountsForAllStudies();
}
