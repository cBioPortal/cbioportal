package org.cbioportal.web.util;

import org.apache.commons.collections.map.MultiKeyMap;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClinicalDataEqualityFilterApplier extends ClinicalDataFilterApplier {
    @Autowired
    public ClinicalDataEqualityFilterApplier(PatientService patientService,
                                             ClinicalDataService clinicalDataService,
                                             StudyViewFilterUtil studyViewFilterUtil) {
        super(patientService, clinicalDataService, studyViewFilterUtil);
    }

    @Override
    public Integer apply(List<ClinicalDataFilter> attributes,
                         MultiKeyMap clinicalDataMap,
                         String entityId,
                         String studyId,
                         Boolean negateFilters) {
        Integer count = 0;

        for (ClinicalDataFilter s : attributes) {
            List<ClinicalData> entityClinicalData = (List<ClinicalData>)clinicalDataMap.get(entityId, studyId);
            List<String> filteredValues = s.getValues().stream().map(DataFilterValue::getValue)
                    .collect(Collectors.toList());
            filteredValues.replaceAll(String::toUpperCase);
            if (entityClinicalData != null) {
                Optional<ClinicalData> clinicalData = entityClinicalData.stream().filter(
                    c -> c.getAttrId().toUpperCase()
                    .equals(s.getAttributeId().toUpperCase())
                ).findFirst();
                if (clinicalData.isPresent() && (negateFilters ^ filteredValues.contains(clinicalData.get().getAttrValue()))) {
                    count++;
                } else if (!clinicalData.isPresent() && (negateFilters ^ filteredValues.contains("NA"))) {
                    count++;
                }
            } else if (negateFilters ^ filteredValues.contains("NA")) {
                count++;
            }
        }

        return count;
    }
}
