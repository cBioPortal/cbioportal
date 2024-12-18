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
        List<String> studyIdsList = new ArrayList<>(sampleIdsByStudyId.size());
        List<String> sampleIdsList = new ArrayList<>(sampleIdsByStudyId.size());
        sampleIdsByStudyId.forEach((studyId, sampleIds) -> {
            sampleIds.forEach(sampleId -> {
                studyIdsList.add(studyId);
                sampleIdsList.add(sampleId);
            });
        });
        List<ClinicalData> clinicalDataItems = clinicalDataService.fetchClinicalData(studyIdsList, sampleIdsList, null, "SAMPLE", "DETAILED");
        Map<List<String>, List<ClinicalData>> clinicalDataItemsGroupedByPatientSample = clinicalDataItems.stream()
            .filter(clinicalData -> sampleIdsByStudyId.get(clinicalData.getStudyId()).contains(clinicalData.getSampleId()))
            .collect(Collectors.groupingBy(clinicalData -> List.of(clinicalData.getStudyId(), clinicalData.getPatientId(), clinicalData.getSampleId())));
        List<SequencedMap<ClinicalAttribute, String>> rows = clinicalDataItemsGroupedByPatientSample
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
                result.putFirst(ClinicalAttribute.SAMPLE_ID, clinicalDataList.getFirst().getSampleId());
                result.putFirst(ClinicalAttribute.PATIENT_ID, clinicalDataList.getFirst().getPatientId());
                return result;
            }).toList();
        SequencedSet<ClinicalAttribute> attributes = rows.stream()
            .flatMap(row -> row.sequencedKeySet().stream()).collect(Collectors.toCollection(LinkedHashSet::new));
        return new ClinicalAttributeData(attributes, rows.iterator());
    }
}
