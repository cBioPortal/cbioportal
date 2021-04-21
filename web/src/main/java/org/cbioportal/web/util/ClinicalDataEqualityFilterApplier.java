package org.cbioportal.web.util;

import java.util.List;

import org.apache.commons.collections.map.MultiKeyMap;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClinicalDataEqualityFilterApplier extends ClinicalDataFilterApplier {
    @Autowired
    public ClinicalDataEqualityFilterApplier(PatientService patientService,
                                             ClinicalDataService clinicalDataService,
                                             StudyViewFilterUtil studyViewFilterUtil) {
        super(patientService, clinicalDataService, studyViewFilterUtil);
    }
    
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;

    @Override
    public Integer apply(List<ClinicalDataFilter> attributes,
                         MultiKeyMap clinicalDataMap,
                         String entityId,
                         String studyId,
                         Boolean negateFilters) {
        return studyViewFilterUtil.getFilteredCountByDataEquality(attributes, clinicalDataMap, entityId, studyId, negateFilters);
    }
}
