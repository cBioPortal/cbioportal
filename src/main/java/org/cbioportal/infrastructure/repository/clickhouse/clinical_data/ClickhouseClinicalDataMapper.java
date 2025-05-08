package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCountItem;

/**
 * Mapper interface for retrieving clinical data from ClickHouse. This interface provides methods to
 * fetch clinical data counts and clinical data for samples and patients.
 */
public interface ClickhouseClinicalDataMapper {

  /**
   * Retrieves clinical data counts based on the study view filter context, attribute IDs, and
   * filtered attribute values.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param attributeIds the list of attribute IDs to filter by
   * @param filteredAttributeValues the list of filtered attribute values
   * @return a list of clinical data count items
   */
  List<ClinicalDataCountItem> getClinicalDataCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<String> attributeIds,
      List<String> filteredAttributeValues);

  /**
   * Retrieves sample clinical data based on the study view filter context and attribute IDs.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param attributeIds the list of attribute IDs to filter by
   * @return a list of sample clinical data
   */
  List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(
      StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);

  /**
   * Retrieves patient clinical data based on the study view filter context and attribute IDs.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param attributeIds the list of attribute IDs to filter by
   * @return a list of patient clinical data
   */
  List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(
      StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);
}
