package org.cbioportal.infrastructure.repository.clickhouse.clinical_event;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;

/**
 * Mapper interface for retrieving clinical event type data from ClickHouse. This interface provides
 * a method to fetch clinical event type counts based on the study view filter context.
 */
public interface ClickhouseClinicalEventMapper {

  /**
   * Retrieves the clinical event type counts based on the study view filter context.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @return a list of clinical event type counts
   */
  List<ClinicalEventTypeCount> getClinicalEventTypeCounts(
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
}
