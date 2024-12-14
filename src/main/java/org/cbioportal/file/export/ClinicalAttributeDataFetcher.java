package org.cbioportal.file.export;

import org.cbioportal.file.model.ClinicalAttribute;
import org.cbioportal.file.model.ClinicalAttributeData;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.service.ClinicalDataService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ClinicalAttributeDataFetcher {

    private final ClinicalDataService clinicalDataService;

    public ClinicalAttributeDataFetcher(ClinicalDataService clinicalDataService) {
        this.clinicalDataService = clinicalDataService;
    }

    public ClinicalAttributeData fetch(Map<String, Set<String>> sampleIdsByStudyId) {
        List<String> studyIds = List.copyOf(sampleIdsByStudyId.keySet());
        List<String> sampleIds = List.copyOf(sampleIdsByStudyId.values().stream().flatMap(Set::stream).toList());
        //all
        List<String> attributeIds = null;
        Iterator<SequencedMap<ClinicalAttribute, String>> rows = clinicalDataService.fetchClinicalData(studyIds, sampleIds, attributeIds, "SAMPLE", "DETAILED").stream()
            .filter(clinicalData -> sampleIdsByStudyId.get(clinicalData.getStudyId()).contains(clinicalData.getSampleId()))
            .collect(Collectors.groupingBy(clinicalData -> new String[]{clinicalData.getStudyId(), clinicalData.getPatientId(), clinicalData.getSampleId()}))
            .values().stream().map(clinicalDataList -> {
                SequencedMap<ClinicalAttribute, String> result = clinicalDataList.stream().collect(Collectors.toMap(
                    clinicalDataItem -> new ClinicalAttribute(
                        clinicalDataItem.getClinicalAttribute().getDisplayName(),
                        clinicalDataItem.getClinicalAttribute().getDescription(),
                        clinicalDataItem.getClinicalAttribute().getDatatype(),
                        clinicalDataItem.getClinicalAttribute().getPriority(),
                        clinicalDataItem.getClinicalAttribute().getAttrId()
                    ),
                    ClinicalData::getAttrValue,
                    (existing, replacement) -> { // Merge function
                        throw new IllegalStateException(
                            "Duplicate key detected: " + existing
                        );
                    },
                    LinkedHashMap::new));
                return result;
            }).iterator();
        SequencedSet<ClinicalAttribute> attributes = StreamSupport.stream(Spliterators.spliteratorUnknownSize(rows, Spliterator.ORDERED), false)
            .flatMap(row -> row.keySet().stream()).collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(
                ClinicalAttribute::attributeId,
                (attr1, att2) -> {
                    if (attr1.equals(ClinicalAttribute.PATIENT_ID.attributeId())) {
                        return -1;
                    }
                    if (att2.equals(ClinicalAttribute.PATIENT_ID.attributeId())) {
                        return 1;
                    }
                    if (attr1.equals(ClinicalAttribute.SAMPLE_ID.attributeId())) {
                        return -1;
                    }
                    if (att2.equals(ClinicalAttribute.SAMPLE_ID.attributeId())) {
                        return 1;
                    }
                    return attr1.compareTo(att2);
                }))));
        return new ClinicalAttributeData(attributes, rows);
    }
}
