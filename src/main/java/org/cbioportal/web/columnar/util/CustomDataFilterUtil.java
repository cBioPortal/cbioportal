package org.cbioportal.web.columnar.util;

import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.Patient;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.CustomDatatype;
import org.cbioportal.web.util.DataBinHelper;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CustomDataFilterUtil {
    private final StudyViewFilterUtil studyViewFilterUtil;
    private final CustomDataService customDataService;
    private final PatientService patientService;

    @Autowired
    public CustomDataFilterUtil(StudyViewFilterUtil studyViewFilterUtil, CustomDataService customDataService, PatientService patientService) {
        this.studyViewFilterUtil = studyViewFilterUtil;
        this.customDataService = customDataService;
        this.patientService = patientService;
    }

    public List<String> extractInvolvedCancerStudies(final StudyViewFilter studyViewFilter) {
        List<String> involvedCancerStudies;
        
        if (studyViewFilter.getStudyIds() != null && !studyViewFilter.getStudyIds().isEmpty()) {
            involvedCancerStudies = studyViewFilter.getStudyIds();
        } else if (studyViewFilter.getSampleIdentifiers() != null && !studyViewFilter.getSampleIdentifiers().isEmpty()) {
            Set<String> studyIdSet = new HashSet<>();
            for (SampleIdentifier sampleIdentifier : studyViewFilter.getSampleIdentifiers()) {
                studyIdSet.add(sampleIdentifier.getStudyId());
            }
            involvedCancerStudies = studyIdSet.stream().toList();
        }
        else {
            involvedCancerStudies = Collections.emptyList();
        }
        
        return involvedCancerStudies;
    }
    public List<CustomSampleIdentifier> extractCustomDataSamples(final StudyViewFilter studyViewFilter) {
        if (studyViewFilter == null) {
            return null;
        }

        if (CollectionUtils.isEmpty(studyViewFilter.getCustomDataFilters())) {
            return null;
        }

        final List<CustomSampleIdentifier> customSamplesFromProperty = studyViewFilter.getCustomDataFilters().stream()
            .flatMap(filter -> {
                List<CustomSampleIdentifier> samples = filter.getSamples();
                return (samples != null) ? samples.stream() : Stream.empty();
            })
            .toList();

        if (!customSamplesFromProperty.isEmpty()) {
            return extractCustomDataSamplesWithoutSession(studyViewFilter, customSamplesFromProperty);
        }
        else {
            return extractCustomDataSamplesWithSession(studyViewFilter);
        }
    }
    
    private List<CustomSampleIdentifier> extractCustomDataSamplesWithSession(final StudyViewFilter studyViewFilter) {
        List<CustomSampleIdentifier> customSampleIdentifiers = new ArrayList<>();
        
        final List<String> attributeIds = studyViewFilter.getCustomDataFilters().stream()
            .map(ClinicalDataFilter::getAttributeId)
            .collect(Collectors.toList());

        final Map<String, CustomDataSession> customDataSessions = customDataService.getCustomDataSessions(attributeIds);

        Map<String, CustomDataSession> customDataSessionById = customDataSessions
            .values()
            .stream()
            .collect(Collectors.toMap(
                CustomDataSession::getId,
                Function.identity()
            ));

        MultiKeyMap<String, String> customDataByStudySampleSession = new MultiKeyMap<>();

        customDataSessionById.values().forEach(customDataSession -> customDataSession
            .getData()
            .getData()
            .forEach(datum -> {
                String value = datum.getValue().toUpperCase();
                if (value.equals("NAN") || value.equals("N/A")) {
                    value = "NA";
                }
                CustomSampleIdentifier customSampleIdentifier = new CustomSampleIdentifier();
                customSampleIdentifier.setStudyId(datum.getStudyId());
                customSampleIdentifier.setSampleId(datum.getSampleId());
                customSampleIdentifiers.add(customSampleIdentifier);
                customDataByStudySampleSession.put(datum.getStudyId(), datum.getSampleId(), customDataSession.getId(), value);
            })
        );

        List<ClinicalDataFilter> equalityFilters = new ArrayList<>();
        List<ClinicalDataFilter> intervalFilters = new ArrayList<>();

        studyViewFilter.getCustomDataFilters().forEach(filter -> {
            String attributeId = filter.getAttributeId();
            if (!customDataSessionById.containsKey(attributeId)) {
                return;
            }
            if (customDataSessionById
                .get(attributeId)
                .getData()
                .getDatatype()
                .equals(CustomDatatype.STRING.name())
            ) {
                equalityFilters.add(filter);
            } else {
                intervalFilters.add(filter);
            }
        });

        List<CustomSampleIdentifier> filtered = new ArrayList<>();
        customSampleIdentifiers.forEach(customSampleIdentifier -> {
            int equalityFilterCount = studyViewFilterUtil.getFilteredCountByDataEquality(equalityFilters, customDataByStudySampleSession,
                customSampleIdentifier.getSampleId(), customSampleIdentifier.getStudyId(), false);
            int intervalFilterCount = getFilteredCountByDataInterval(intervalFilters, customDataByStudySampleSession,
                customSampleIdentifier.getSampleId(), customSampleIdentifier.getStudyId());
            if (equalityFilterCount == equalityFilters.size()
                && intervalFilterCount == intervalFilters.size()
            ) {
                filtered.add(customSampleIdentifier);
            }
            else {
                customSampleIdentifier.setIsFilteredOut(true);
                filtered.add(customSampleIdentifier);
            }
        });

        return filtered;
    }
    
    private List<CustomSampleIdentifier> extractCustomDataSamplesWithoutSession(final StudyViewFilter studyViewFilter, List<CustomSampleIdentifier> customSamplesFromProperty) {
        List<CustomSampleIdentifier> customSampleIdentifiers = new ArrayList<>(customSamplesFromProperty);

        List<ClinicalDataFilter> equalityFilters = new ArrayList<>();
        List<ClinicalDataFilter> intervalFilters = new ArrayList<>();

        studyViewFilter.getCustomDataFilters().forEach(filter -> {
            if (filter.getDatatype()
                .equals(CustomDatatype.STRING.name())
            ) {
                equalityFilters.add(filter);
            } else {
                intervalFilters.add(filter);
            }
        });

        MultiKeyMap<String, String> customDataByStudySampleName = new MultiKeyMap<>();

        studyViewFilter.getCustomDataFilters().stream().forEach(filter ->
            filter.getSamples().forEach(datum -> {
                String value = datum.getValue().toUpperCase();
                if (value.equals("NAN") || value.equals("N/A")) {
                    value = "NA";
                }
                customDataByStudySampleName.put(datum.getStudyId(), datum.getSampleId(), filter.getDisplayName(), value);
            })
        );

        List<CustomSampleIdentifier> filtered = new ArrayList<>();
        customSampleIdentifiers.forEach(customSampleIdentifier -> {
            int equalityFilterCount = getFilteredCountByDataEqualityWithStudySampleNameMap(equalityFilters, customDataByStudySampleName,
                customSampleIdentifier.getSampleId(), customSampleIdentifier.getStudyId(), false);
            int intervalFilterCount = getFilteredCountByDataIntervalWithStudySampleNameMap(intervalFilters, customDataByStudySampleName,
                customSampleIdentifier.getSampleId(), customSampleIdentifier.getStudyId());
            if (equalityFilterCount == equalityFilters.size()
                && intervalFilterCount == intervalFilters.size()
            ) {
                filtered.add(customSampleIdentifier);
            }
            else {
                customSampleIdentifier.setIsFilteredOut(true);
                filtered.add(customSampleIdentifier);
            }
        });
        return filtered;
    }

    private Integer getFilteredCountByDataEqualityWithStudySampleNameMap(List<ClinicalDataFilter> attributes, MultiKeyMap<String, String> clinicalDataMap,
                                                   String entityId, String studyId, boolean negateFilters) {
        Integer count = 0;
        for (ClinicalDataFilter s : attributes) {
            List<String> filteredValues = s.getValues()
                .stream()
                .map(DataFilterValue::getValue)
                .collect(Collectors.toList());
            filteredValues.replaceAll(String::toUpperCase);
            if (clinicalDataMap.containsKey(studyId, entityId, s.getDisplayName())) {
                String value = clinicalDataMap.get(studyId, entityId, s.getDisplayName());
                if (negateFilters ^ filteredValues.contains(value)) {
                    count++;
                }
            } else if (negateFilters ^ filteredValues.contains("NA")) {
                count++;
            }
        }
        return count;
    }

    private <S> Integer getFilteredCountByDataInterval(List<ClinicalDataFilter> attributes, MultiKeyMap<String, S> clinicalDataMap,
                                                       String entityId, String studyId) {
        int count = 0;

        for (ClinicalDataFilter filter : attributes) {
            if (clinicalDataMap.containsKey(studyId, entityId, filter.getAttributeId())) {
                String attrValue = (String) clinicalDataMap.get(studyId, entityId, filter.getAttributeId());
                Range<BigDecimal> rangeValue = calculateRangeValueForAttr(attrValue);

                // find range filters
                List<Range<BigDecimal>> ranges = filter.getValues().stream()
                    .map(this::calculateRangeValueForFilter)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                // find special value filters
                List<String> specialValues = filter.getValues().stream()
                    .filter(f -> f.getValue() != null)
                    .map(f -> f.getValue().toUpperCase())
                    .collect(Collectors.toList());

                if (rangeValue != null) {
                    if (ranges.stream().anyMatch(r -> r.encloses(rangeValue))) {
                        count++;
                    }
                }
                else if (specialValues.contains(attrValue.toUpperCase())) {
                    count++;
                }
            } else if (containsNA(filter)) {
                count++;
            }
        }

        return count;
    }

    private Integer getFilteredCountByDataIntervalWithStudySampleNameMap(List<ClinicalDataFilter> attributes, MultiKeyMap<String, String> clinicalDataMap,
                                                   String entityId, String studyId) {
        int count = 0;

        for (ClinicalDataFilter filter : attributes) {
            if (clinicalDataMap.containsKey(studyId, entityId, filter.getDisplayName())) {
                String attrValue = clinicalDataMap.get(studyId, entityId, filter.getDisplayName());
                Range<BigDecimal> rangeValue = calculateRangeValueForAttr(attrValue);

                // find range filters
                List<Range<BigDecimal>> ranges = filter.getValues().stream()
                    .map(this::calculateRangeValueForFilter)
                    .filter(Objects::nonNull)
                    .toList();

                // find special value filters
                List<String> specialValues = filter.getValues().stream()
                    .filter(f -> f.getValue() != null)
                    .map(f -> f.getValue().toUpperCase())
                    .toList();

                if ((rangeValue != null && ranges.stream().anyMatch(r -> r.encloses(rangeValue))) ||
                    (rangeValue == null && specialValues.contains(attrValue.toUpperCase()))) {
                    count++;
                }
            } else if (Boolean.TRUE.equals(containsNA(filter))) {
                count++;
            }
        }

        return count;
    }

    private Range<BigDecimal> calculateRangeValueForAttr(String attrValue) {
        if (attrValue == null) {
            return null;
        }

        BigDecimal min = null;
        BigDecimal max = null;

        String value = attrValue.trim();

        String lte = "<=";
        String lt = "<";
        String gte = ">=";
        String gt = ">";

        boolean startInclusive = true;
        boolean endInclusive = true;

        try {
            if (value.startsWith(lte)) {
                max = new BigDecimal(value.substring(lte.length()));
            }
            else if (value.startsWith(lt)) {
                max = new BigDecimal(value.substring(lt.length()));
                endInclusive = false;
            }
            else if (value.startsWith(gte)) {
                min = new BigDecimal(value.substring(gte.length()));
            }
            else if (value.startsWith(gt)) {
                min = new BigDecimal(value.substring(gt.length()));
                startInclusive = false;
            }
            else {
                min = max = new BigDecimal(attrValue);
            }
        } catch (Exception e) {
            // invalid range -- TODO: also support ranges like 20-30?
            return null;
        }

        return DataBinHelper.calcRange(min, startInclusive, max, endInclusive);
    }

    private Range<BigDecimal> calculateRangeValueForFilter(DataFilterValue filterValue) {
        BigDecimal start = filterValue.getStart();
        BigDecimal end = filterValue.getEnd();

        // default: (start, end]
        boolean startInclusive = false;
        boolean endInclusive = true;

        // special case: end == start (both inclusive)
        if (end != null && end.equals(start)) {
            startInclusive = true;
        }

        return DataBinHelper.calcRange(start, startInclusive, end, endInclusive);
    }

    private Boolean containsNA(ClinicalDataFilter filter) {
        return filter.getValues().stream().anyMatch(
            r -> r.getValue() != null && r.getValue().toUpperCase().equals("NA"));
    }

    public List<ClinicalDataCountItem> getCustomDataCounts(List<SampleIdentifier> filteredSampleIdentifiers, Map<String, CustomDataSession> customDataSessions) {
        Map<String, SampleIdentifier> customSamplesMap = filteredSampleIdentifiers.stream()
            .collect(Collectors.toMap(sampleIdentifier -> studyViewFilterUtil.getCaseUniqueKey(
                sampleIdentifier.getStudyId(),
                sampleIdentifier.getSampleId()
            ), Function.identity()));

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);

        long patientCustomDataSessionsCount = customDataSessions.values().stream()
            .filter(customDataSession -> customDataSession.getData().getPatientAttribute()).count();
        List<Patient> patients = new ArrayList<>();
        if (patientCustomDataSessionsCount > 0) {
            patients.addAll(patientService.getPatientsOfSamples(studyIds, sampleIds));
        }

        return studyViewFilterUtil.getClinicalDataCountsFromCustomData(customDataSessions.values(),
            customSamplesMap, patients);
    }
}
