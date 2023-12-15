package org.cbioportal.service.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.cbioportal.model.ClinicalAttribute;
import org.springframework.stereotype.Component;

@Component
public class ClinicalAttributeUtil {

    public void extractCategorizedClinicalAttributes(
        List<ClinicalAttribute> clinicalAttributes,
        List<String> sampleAttributeIds, 
        List<String> patientAttributeIds,
        List<String> conflictingPatientAttributeIds
    ) {

        Set<String> sampleAttributeIdsSet = new HashSet<String>();
        Set<String> patientAttributeIdsSet = new HashSet<String>();
        Set<String> conflictingPatientAttributeIdsSet = new HashSet<String>();

        Map<String, Map<Boolean, List<ClinicalAttribute>>> groupedAttributesByIdAndType = clinicalAttributes
            .stream()
            .collect(Collectors.groupingBy(
                ClinicalAttribute::getAttrId,
                Collectors.groupingBy(ClinicalAttribute::getPatientAttribute)
            ));

        groupedAttributesByIdAndType.entrySet().forEach(entry -> {
            if (entry.getValue().keySet().size() == 1) {
                entry.getValue().entrySet().stream().forEach(x -> {
                    if (x.getKey()) {
                        patientAttributeIdsSet.add(entry.getKey());
                    } else {
                        sampleAttributeIdsSet.add(entry.getKey());
                    }
                });
            } else {
                entry.getValue().entrySet().forEach(x -> {
                    if (x.getKey()) {
                        conflictingPatientAttributeIdsSet.add(entry.getKey());
                    } else {
                        sampleAttributeIdsSet.add(entry.getKey());
                    }
                });
            }
        });

        sampleAttributeIds.addAll(sampleAttributeIdsSet);
        patientAttributeIds.addAll(patientAttributeIdsSet);
        conflictingPatientAttributeIds.addAll(conflictingPatientAttributeIdsSet);
    }
}
