package org.cbioportal.web.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.model.Binnable;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Patient;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.StructuralVariantFilterQuery;
import org.cbioportal.model.StructuralVariantSpecialValue;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.service.util.CustomDataValue;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class StudyViewFilterUtil {

    private final GeneService geneService;
    
    @Autowired
    public StudyViewFilterUtil(GeneService geneService) {
        this.geneService = geneService;
    }
    
    public StudyViewFilterUtil() {
        geneService = null;
    }

    public void extractStudyAndSampleIds(
        List<SampleIdentifier> sampleIdentifiers,
        List<String> studyIds,
        List<String> sampleIds
    ) {
        for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
            studyIds.add(sampleIdentifier.getStudyId());
            sampleIds.add(sampleIdentifier.getSampleId());
        }
    }

    public void removeSelfFromFilter(String attributeId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getClinicalDataFilters() != null) {
            studyViewFilter.getClinicalDataFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }

    public void removeSelfFromGenomicDataFilter(String hugoGeneSymbol, String profileType, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getGenomicDataFilters() != null) {
            studyViewFilter.getGenomicDataFilters().removeIf(f ->
                f.getHugoGeneSymbol().equals(hugoGeneSymbol) && f.getProfileType().equals(profileType)
            );
        }
    }

    public void removeSelfFromMutationDataFilter(String hugoGeneSymbol, String profileType, MutationOption categorization, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getMutationDataFilters() != null) {
            studyViewFilter.getMutationDataFilters().removeIf(f ->
                f.getHugoGeneSymbol().equals(hugoGeneSymbol) &&
                    f.getProfileType().equals(profileType) &&
                    f.getCategorization().equals(categorization)
            );
        }
    }

    public void removeSelfFromGenericAssayFilter(String stableId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getGenericAssayDataFilters() != null) {
            studyViewFilter.getGenericAssayDataFilters().removeIf(f -> f.getStableId().equals(stableId));
        }
    }

    public void removeSelfCustomDataFromFilter(String attributeId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getCustomDataFilters() != null) {
            studyViewFilter.getCustomDataFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }

    public String getCaseUniqueKey(String studyId, String caseId) {
        return studyId + caseId;
    }

    public <S extends DataFilter> String getDataFilterUniqueKey(S dataFilter) {
        if (dataFilter instanceof GenomicDataFilter genomicDataFilter) {
            return genomicDataFilter.getHugoGeneSymbol() + genomicDataFilter.getProfileType();
        } else if (dataFilter instanceof GenericAssayDataFilter genericAssayDataFilter) {
            return genericAssayDataFilter.getStableId() + genericAssayDataFilter.getProfileType();
        }
        return null;
    }

    public String getMutationDataFilterUniqueKey(MutationDataFilter mutationDataFilter) {
        return mutationDataFilter.getHugoGeneSymbol() + mutationDataFilter.getProfileType();
    }

    public <S extends DataBinFilter> String getDataBinFilterUniqueKey(S dataBinFilter) {
        if (dataBinFilter instanceof GenomicDataBinFilter genomicDataBinFilter) {
            return genomicDataBinFilter.getHugoGeneSymbol() + genomicDataBinFilter.getProfileType();
        } else if (dataBinFilter instanceof GenericAssayDataBinFilter genericAssayDataBinFilter) {
            return genericAssayDataBinFilter.getStableId() + genericAssayDataBinFilter.getProfileType();
        }
        return null;
    }

    public Map<String, List<SampleList>> categorizeSampleLists(List<SampleList> sampleLists) {
        return sampleLists.stream().collect(Collectors.groupingBy(sampleList -> {
            return sampleList.getStableId().replace(sampleList.getCancerStudyIdentifier() + "_", "");
        }));
    }

    public <S> Integer getFilteredCountByDataEquality(List<ClinicalDataFilter> attributes, MultiKeyMap<String, S> clinicalDataMap,
                                                      String entityId, String studyId, boolean negateFilters) {
        Integer count = 0;
        for (ClinicalDataFilter s : attributes) {
            List<String> filteredValues = s.getValues()
                .stream()
                .map(DataFilterValue::getValue)
                .collect(Collectors.toList());
            filteredValues.replaceAll(String::toUpperCase);
            if (clinicalDataMap.containsKey(studyId, entityId, s.getAttributeId())) {
                S value = clinicalDataMap.get(studyId, entityId, s.getAttributeId());
                if (value instanceof String) {
                    if (negateFilters ^ filteredValues.contains(value)) {
                        count++;
                    }
                } else if (value instanceof List &&
                    negateFilters ^ filteredValues.stream().anyMatch(((List<?>) value)::contains)) {
                    count++;
                }
            } else if (negateFilters ^ filteredValues.contains("NA")) {
                count++;
            }
        }
        return count;
    }

    public List<ClinicalDataCountItem> getClinicalDataCountsFromCustomData(Collection<CustomDataSession> customDataSessions,
                                                                           Map<String, SampleIdentifier> filteredSamplesMap, List<Patient> patients) {
        int totalSamplesCount = filteredSamplesMap.keySet().size();
        int totalPatientsCount = patients.size();

        return customDataSessions.stream().map(customDataSession -> {

            Map<String, List<CustomDataValue>> groupedDatabyValue = customDataSession.getData().getData().stream()
                .filter(datum -> filteredSamplesMap
                    .containsKey(getCaseUniqueKey(datum.getStudyId(), datum.getSampleId()))
                ).collect(Collectors.groupingBy(CustomDataValue::getValue));

            ClinicalDataCountItem clinicalDataCountItem = new ClinicalDataCountItem();
            clinicalDataCountItem.setAttributeId(customDataSession.getId());

            List<ClinicalDataCount> clinicalDataCounts = groupedDatabyValue.entrySet().stream()
                .map(entry -> {
                    long count = entry.getValue().stream().map(datum -> {
                        return getCaseUniqueKey(datum.getStudyId(),
                            customDataSession.getData().getPatientAttribute()
                                ? datum.getPatientId()
                                : datum.getSampleId());

                    }).distinct().count();
                    ClinicalDataCount dataCount = new ClinicalDataCount();
                    dataCount.setAttributeId(customDataSession.getId());
                    dataCount.setValue(entry.getKey());
                    dataCount.setCount(Math.toIntExact(count));
                    return dataCount;
                })
                .filter(c -> !c.getValue().equalsIgnoreCase("NA") && !c.getValue().equalsIgnoreCase("NAN")
                    && !c.getValue().equalsIgnoreCase("N/A"))
                .collect(Collectors.toList());

            int totalCount = clinicalDataCounts.stream().mapToInt(ClinicalDataCount::getCount).sum();
            int naCount = 0;
            if (customDataSession.getData().getPatientAttribute()) {
                naCount = totalPatientsCount - totalCount;
            } else {
                naCount = totalSamplesCount - totalCount;
            }
            if (naCount > 0) {
                ClinicalDataCount clinicalDataCount = new ClinicalDataCount();
                clinicalDataCount.setAttributeId(customDataSession.getId());
                clinicalDataCount.setValue("NA");
                clinicalDataCount.setCount(naCount);
                clinicalDataCounts.add(clinicalDataCount);
            }

            clinicalDataCountItem.setCounts(clinicalDataCounts);
            return clinicalDataCountItem;
        }).collect(Collectors.toList());
    }
    
    public boolean isSingleStudyUnfiltered(StudyViewFilter filter) {
        return isSingleStudy(filter) && isUnfilteredQuery(filter);
    }

    public boolean isSingleStudy(StudyViewFilter filter) {
        return filter.getStudyIds() != null &&
            filter.getStudyIds().size() == 1;
    }
    
    public boolean isUnfilteredQuery(StudyViewFilter filter) {
        return filter.getStudyIds() != null &&
            (filter.getClinicalDataFilters() == null || filter.getClinicalDataFilters().isEmpty()) &&
            (filter.getGeneFilters() == null || filter.getGeneFilters().isEmpty()) &&
            (filter.getSampleTreatmentFilters() == null || filter.getSampleTreatmentFilters().getFilters().isEmpty()) &&
            (filter.getPatientTreatmentFilters() == null || filter.getPatientTreatmentFilters().getFilters().isEmpty()) &&
            (filter.getGenomicProfiles() == null || filter.getGenomicProfiles().isEmpty()) &&
            (filter.getGenomicDataFilters() == null || filter.getGenomicDataFilters().isEmpty()) &&
            (filter.getGenericAssayDataFilters() == null || filter.getGenericAssayDataFilters().isEmpty()) &&
            (filter.getCaseLists() == null || filter.getCaseLists().isEmpty()) &&
            (filter.getCustomDataFilters() == null || filter.getCustomDataFilters().isEmpty()) &&
            (filter.getMutationDataFilters() == null || filter.getMutationDataFilters().isEmpty());
    }

    public boolean shouldSkipFilterForClinicalDataBins(StudyViewFilter filter) {
        // if everything other than study ids and sample identifiers is null,
        // we can skip the filter for data bin calculation
        return (
            filter != null &&
                filter.getClinicalDataFilters() == null &&
                filter.getClinicalEventFilters() == null &&
                filter.getMutationDataFilters() == null &&
                filter.getStructuralVariantFilters() == null &&
                filter.getGeneFilters() == null &&
                filter.getSampleTreatmentFilters() == null &&
                filter.getPatientTreatmentFilters() == null &&
                filter.getPatientTreatmentGroupFilters() == null &&
                filter.getSampleTreatmentGroupFilters() == null &&
                filter.getPatientTreatmentTargetFilters() == null &&
                filter.getSampleTreatmentTargetFilters() == null &&
                filter.getGenomicProfiles() == null &&
                filter.getGenomicDataFilters() == null &&
                filter.getGenericAssayDataFilters() == null &&
                filter.getCaseLists() == null &&
                filter.getCustomDataFilters() == null
        );
    }

    public List<Binnable> filterClinicalData(
        List<Binnable> unfilteredClinicalDataForSamples,
        List<Binnable> unfilteredClinicalDataForPatients,
        List<Binnable> unfilteredClinicalDataForConflictingPatientAttributes,
        List<String> studyIds,
        List<String> sampleIds,
        List<String> studyIdsOfPatients,
        List<String> patientIds,
        List<String> sampleAttributeIds,
        List<String> patientAttributeIds,
        List<String> conflictingPatientAttributes
    ) {
        List<Binnable> combinedResult = new ArrayList<>();

        Map<String, String> patientIdToStudyId = null;

        if (CollectionUtils.isNotEmpty(sampleAttributeIds)) {
            // create lookups for faster filtering
            Map<String, String> sampleIdToStudyId = mapCaseToStudy(sampleIds, studyIds);
            Map<String, Boolean> sampleAttributeIdLookup = listToMap(sampleAttributeIds);

            combinedResult.addAll(
                filterClinicalDataByStudyAndSampleAndAttribute(
                    unfilteredClinicalDataForSamples,
                    sampleIdToStudyId,
                    sampleAttributeIdLookup
                )
            );
        }

        if (CollectionUtils.isNotEmpty(patientAttributeIds)) {
            // create lookups for faster filtering
            Map<String, Boolean> patientAttributeIdLookup = listToMap(patientAttributeIds);
            patientIdToStudyId = mapCaseToStudy(patientIds, studyIdsOfPatients);

            combinedResult.addAll(
                filterClinicalDataByStudyAndPatientAndAttribute(
                    unfilteredClinicalDataForPatients,
                    patientIdToStudyId,
                    patientAttributeIdLookup
                )
            );
        }

        if (CollectionUtils.isNotEmpty(conflictingPatientAttributes)) {
            // create lookups for faster filtering
            Map<String, Boolean> conflictingPatientAttributeIdLookup = listToMap(conflictingPatientAttributes);
            if (patientIdToStudyId == null) {
                patientIdToStudyId = mapCaseToStudy(patientIds, studyIdsOfPatients);
            }

            combinedResult.addAll(
                filterClinicalDataByStudyAndPatientAndAttribute(
                    unfilteredClinicalDataForConflictingPatientAttributes,
                    patientIdToStudyId,
                    conflictingPatientAttributeIdLookup
                )
            );
        }

        return combinedResult;
    }



    private Map<String, Integer> getStringIntegerMap(List<String> hugoGeneSymbols) {
        Map<String, Integer> symbolToEntrezGeneId = geneService
            .fetchGenes(new ArrayList<>(hugoGeneSymbols),
                GeneIdType.HUGO_GENE_SYMBOL.name(), Projection.SUMMARY.name())
            .stream()
            .collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));
        return symbolToEntrezGeneId;
    }

    public List<StructuralVariantFilterQuery> resolveEntrezGeneIds(List<StructuralVariantFilterQuery> structVarQueries) {

        List<String> hugoGeneSymbols = structVarQueries
            .stream()
            .flatMap(q -> Stream.of(q.getGene1Query(), q.getGene2Query()))
            .filter(structVarIdentifier -> structVarIdentifier.getHugoSymbol() != null)
            .map(structVarIdentifier -> structVarIdentifier.getHugoSymbol())
            .collect(Collectors.toList());

        Map<String, Integer> symbolToEntrezGeneId = getStringIntegerMap(hugoGeneSymbols);

        // Add Entrez gene ids to the queries.
        structVarQueries.forEach(structVarQuery -> {
            structVarQuery.getGene1Query().setEntrezId(
                symbolToEntrezGeneId.getOrDefault(structVarQuery.getGene1Query().getHugoSymbol(), null)
            );
            structVarQuery.getGene2Query().setEntrezId(
                symbolToEntrezGeneId.getOrDefault(structVarQuery.getGene2Query().getHugoSymbol(), null)
            );
        });

        // Remove any genes where the Entrez gene id is needed, but translation failed.
        structVarQueries.removeIf(
            q -> (q.getGene1Query().getSpecialValue() != StructuralVariantSpecialValue.NO_GENE
                && q.getGene1Query().getSpecialValue() != StructuralVariantSpecialValue.ANY_GENE
                && q.getGene1Query().getEntrezId() == null)
                || (q.getGene2Query().getSpecialValue() != StructuralVariantSpecialValue.NO_GENE
                && q.getGene2Query().getSpecialValue() != StructuralVariantSpecialValue.ANY_GENE
                && q.getGene2Query().getEntrezId() == null)
        );

        return structVarQueries;
    }

    private List<Binnable> filterClinicalDataByStudyAndSampleAndAttribute(
        List<Binnable> clinicalData,
        Map<String, String> sampleToStudyId,
        Map<String, Boolean> attributeIdLookup
    ) {
        return clinicalData
            .stream()
            .filter(d ->
                sampleToStudyId.getOrDefault(generateSampleToStudyKey(d), "").equals(d.getStudyId()) &&
                    attributeIdLookup.getOrDefault(d.getAttrId(), false)
            )
            .collect(Collectors.toList());
    }

    private List<Binnable> filterClinicalDataByStudyAndPatientAndAttribute(
        List<Binnable> clinicalData,
        Map<String, String> patientToStudyId,
        Map<String, Boolean> attributeIdLookup
    ) {
        return clinicalData
            .stream()
            .filter(d ->
                patientToStudyId.getOrDefault(generatePatientToStudyKey(d), "").equals(d.getStudyId()) &&
                    attributeIdLookup.getOrDefault(d.getAttrId(), false)
            )
            .collect(Collectors.toList());
    }

    private <T> Map<T, Boolean> listToMap(List<T> list) {
        return list.stream().collect(Collectors.toMap(s -> s, s -> true, (s1, s2) -> s1));
    }

    private Map<String, String> mapCaseToStudy(List<String> caseIds, List<String> studyIds) {
        Map<String, String> caseToStudy = new HashMap<>();

        for (int i = 0; i < caseIds.size(); i++) {
            String studyId = studyIds.get(i);
            String caseId = caseIds.get(i);
            String key = generateCaseToStudyKey(studyId, caseId);
            caseToStudy.put(key, studyId);
        }

        return caseToStudy;
    }

    private String generateSampleToStudyKey(Binnable clinicalData) {
        return generateCaseToStudyKey(clinicalData.getStudyId(), clinicalData.getSampleId());
    }

    private String generatePatientToStudyKey(Binnable clinicalData) {
        return generateCaseToStudyKey(clinicalData.getStudyId(), clinicalData.getPatientId());
    }

    private String generateCaseToStudyKey(String studyId, String caseId) {
        return studyId + ":" + caseId;
    }

    public SampleIdentifier buildSampleIdentifier(String studyId, String sampleId) {
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setStudyId(studyId);
        sampleIdentifier.setSampleId(sampleId);
        return sampleIdentifier;
    }

    public List<ClinicalData> transformSampleIdentifiersToClinicalData(List<SampleIdentifier> sampleIdentifiers, String attributeId, String attributeValue) {
        return sampleIdentifiers
            .stream()
            .map(sampleIdentifier -> {
                ClinicalData clinicalData = new ClinicalData();
                clinicalData.setAttrId(attributeId);
                clinicalData.setAttrValue(attributeValue);
                clinicalData.setSampleId(sampleIdentifier.getSampleId());
                clinicalData.setStudyId(sampleIdentifier.getStudyId());
                return clinicalData;
            }).collect(Collectors.toList());
    }
}
