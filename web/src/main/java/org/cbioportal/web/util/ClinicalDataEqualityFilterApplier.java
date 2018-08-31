package org.cbioportal.web.util;

import org.apache.commons.collections.map.MultiKeyMap;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.parameter.ClinicalDataEqualityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClinicalDataEqualityFilterApplier extends ClinicalDataFilterApplier<ClinicalDataEqualityFilter>
{
    @Autowired
    public ClinicalDataEqualityFilterApplier(PatientService patientService, 
                                             ClinicalDataService clinicalDataService, 
                                             SampleService sampleService,
                                             StudyViewFilterUtil studyViewFilterUtil) 
    {
        super(patientService, clinicalDataService, sampleService, studyViewFilterUtil);
    }
    
    @Override
    public Integer apply(List<ClinicalDataEqualityFilter> attributes,
                         MultiKeyMap clinicalDataMap,
                         String entityId,
                         String studyId,
                         Boolean negateFilters)
    {
        Integer count = 0;

        for (ClinicalDataEqualityFilter s : attributes) {
            List<ClinicalData> entityClinicalData = (List<ClinicalData>)clinicalDataMap.get(entityId, studyId);
            if (entityClinicalData != null) {
                Optional<ClinicalData> clinicalData = entityClinicalData.stream().filter(c -> c.getAttrId()
                    .equals(s.getAttributeId())).findFirst();
                if (clinicalData.isPresent() && (negateFilters ^ s.getValues().contains(clinicalData.get().getAttrValue()))) {
                    count++;
                } else if (!clinicalData.isPresent() && (negateFilters ^ s.getValues().contains("NA"))) {
                    count++;
                }
            } else if (negateFilters ^ s.getValues().contains("NA")) {
                count++;
            }
        }

        return count;
    }
}
