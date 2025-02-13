package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface ClickhouseClinicalDataMapper {
    List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds, List<String> filteredAttributeValues);
    List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);
    List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);
}
