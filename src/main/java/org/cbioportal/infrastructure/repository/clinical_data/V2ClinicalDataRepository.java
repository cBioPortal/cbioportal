package org.cbioportal.infrastructure.repository.clinical_data;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class V2ClinicalDataRepository implements org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository {

    private static final List<String> FILTERED_CLINICAL_ATTR_VALUES = Collections.emptyList();

    private final V2ClinicalDataMapper mapper;

    public V2ClinicalDataRepository(V2ClinicalDataMapper mapper) {
        this.mapper = mapper;
    }


    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
        return mapper.getPatientClinicalDataFromStudyViewFilter(studyViewFilterContext, filteredAttributes);
    }

    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
        return mapper.getSampleClinicalDataFromStudyViewFilter(studyViewFilterContext, filteredAttributes);
    }

    @Override
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
        return mapper.getClinicalDataCounts(studyViewFilterContext, filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES);
    }
}
