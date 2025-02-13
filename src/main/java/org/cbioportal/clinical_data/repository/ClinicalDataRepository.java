package org.cbioportal.clinical_data.repository;

import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface ClinicalDataRepository {
    List<ClinicalData> getPatientClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);
    List<ClinicalData> getSampleClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);
    List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilterContext studyViewFilterContext,
                                                      List<String> filteredAttributes);


}
