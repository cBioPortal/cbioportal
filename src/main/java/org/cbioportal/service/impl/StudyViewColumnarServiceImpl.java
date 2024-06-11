package org.cbioportal.service.impl;

import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.model.*;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.enums.ClinicalAttributeDataType;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.CustomDatatype;
import org.cbioportal.web.util.DataBinHelper;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StudyViewColumnarServiceImpl implements StudyViewColumnarService {

    private final Map<String, List<String>> clinicalAttributeNameMap = new HashMap<>();


    private final StudyViewRepository studyViewRepository;
    private final SampleService sampleService;
    private final StudyViewFilterUtil studyViewFilterUtil;
    private final CustomDataService customDataService;
    private final DataBinHelper dataBinHelper;

    Function<Sample, SampleIdentifier> sampleToSampleIdentifier = new Function<Sample, SampleIdentifier>() {

        public SampleIdentifier apply(Sample sample) {
            return studyViewFilterUtil.buildSampleIdentifier(sample.getCancerStudyIdentifier(), sample.getStableId());
        }
    };
    
    @Autowired
    public StudyViewColumnarServiceImpl(StudyViewRepository studyViewRepository, SampleService sampleService, StudyViewFilterUtil studyViewFilterUtil, CustomDataService customDataService, DataBinHelper dataBinHelper) {
        this.studyViewRepository = studyViewRepository;
        this.sampleService = sampleService;
        this.studyViewFilterUtil = studyViewFilterUtil;
        this.customDataService = customDataService;
        this.dataBinHelper = dataBinHelper;
    }
    
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        List<SampleIdentifier> customDataSamples = extractCustomDataSamples(studyViewFilter);
        return studyViewRepository.getFilteredSamples(studyViewFilter, categorizedClinicalDataCountFilter, customDataSamples);    
    }
    
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        List<SampleIdentifier> customDataSamples = extractCustomDataSamples(studyViewFilter);
        return studyViewRepository.getMutatedGenes(studyViewFilter, categorizedClinicalDataCountFilter, customDataSamples);
    }

    @Override
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);

        return studyViewRepository.getClinicalDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, filteredAttributes)
            .stream().collect(Collectors.groupingBy(ClinicalDataCount::getAttributeId))
            .entrySet().parallelStream().map(e -> {
                ClinicalDataCountItem item = new ClinicalDataCountItem();
                item.setAttributeId(e.getKey());
                item.setCounts(e.getValue());
                return item;
            }).collect(Collectors.toList());
    }

    private List<SampleIdentifier> extractCustomDataSamples(final StudyViewFilter studyViewFilter) {
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        if (studyViewFilter == null) {
            return sampleIdentifiers;
        }
        
        sampleIdentifiers = sampleService.getAllSamplesInStudies(studyViewFilter.getStudyIds(), Projection.ID.name(),
            null, null, null, null).stream().map(sampleToSampleIdentifier).collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(studyViewFilter.getCustomDataFilters()) || sampleIdentifiers.isEmpty()) {
            return sampleIdentifiers;
        }

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

        List<SampleIdentifier> filtered = new ArrayList<>();
        sampleIdentifiers.forEach(sampleIdentifier -> {
            int equalityFilterCount = studyViewFilterUtil.getFilteredCountByDataEquality(equalityFilters, customDataByStudySampleSession,
                sampleIdentifier.getSampleId(), sampleIdentifier.getStudyId(), false);
            int intervalFilterCount = getFilteredCountByDataInterval(intervalFilters, customDataByStudySampleSession,
                sampleIdentifier.getSampleId(), sampleIdentifier.getStudyId(), false);
            if (equalityFilterCount == equalityFilters.size()
                && intervalFilterCount == intervalFilters.size()
            ) {
                filtered.add(sampleIdentifier);
            }
        });

        return filtered;
    }

    private <S> Integer getFilteredCountByDataInterval(List<ClinicalDataFilter> attributes, MultiKeyMap<String, S> clinicalDataMap,
                                                       String entityId, String studyId, boolean negateFilters) {
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
                    if (negateFilters ^ ranges.stream().anyMatch(r -> r.encloses(rangeValue))) {
                        count++;
                    }
                }
                else if (negateFilters ^ specialValues.contains(attrValue.toUpperCase())) {
                    count++;
                }
            } else if (negateFilters ^ containsNA(filter)) {
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

        return dataBinHelper.calcRange(min, startInclusive, max, endInclusive);
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

        return dataBinHelper.calcRange(start, startInclusive, end, endInclusive);
    }

    private Boolean containsNA(ClinicalDataFilter filter) {
        return filter.getValues().stream().anyMatch(
            r -> r.getValue() != null && r.getValue().toUpperCase().equals("NA"));
    }

    private CategorizedClinicalDataCountFilter extractClinicalDataCountFilters(final StudyViewFilter studyViewFilter) {
        if(clinicalAttributeNameMap.isEmpty()) {
            buildClinicalAttributeNameMap();
        }

        if(studyViewFilter.getClinicalDataFilters() == null) {
            return CategorizedClinicalDataCountFilter.getBuilder().build();
        }

        final String patientCategoricalKey = ClinicalAttributeDataSource.PATIENT.getValue() + ClinicalAttributeDataType.CATEGORICAL.getValue();
        final String patientNumericKey = ClinicalAttributeDataSource.PATIENT.getValue() + ClinicalAttributeDataType.NUMERIC.getValue();
        final String sampleCategoricalKey = ClinicalAttributeDataSource.SAMPLE.getValue() + ClinicalAttributeDataType.CATEGORICAL.getValue();
        final String sampleNumericKey = ClinicalAttributeDataSource.SAMPLE.getValue() + ClinicalAttributeDataType.NUMERIC.getValue();

        return CategorizedClinicalDataCountFilter.getBuilder()
            .setPatientCategoricalClinicalDataFilters(studyViewFilter.getClinicalDataFilters()
                .stream().filter(clinicalDataFilter -> clinicalAttributeNameMap.get(patientCategoricalKey).contains(clinicalDataFilter.getAttributeId()))
                .collect(Collectors.toList()))
            .setPatientNumericalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                .filter(clinicalDataFilter -> clinicalAttributeNameMap.get(patientNumericKey).contains(clinicalDataFilter.getAttributeId()))
                .collect(Collectors.toList()))
            .setSampleCategoricalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                .filter(clinicalDataFilter -> clinicalAttributeNameMap.get(sampleCategoricalKey).contains(clinicalDataFilter.getAttributeId()))
                .collect(Collectors.toList()))
            .setSampleNumericalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                .filter(clinicalDataFilter -> clinicalAttributeNameMap.get(sampleNumericKey).contains(clinicalDataFilter.getAttributeId()))
                .collect(Collectors.toList()))
            .build();
    }

    private void buildClinicalAttributeNameMap() {
        List<ClinicalAttributeDataSource> clinicalAttributeDataSources = List.of(ClinicalAttributeDataSource.values());
        for(ClinicalAttributeDataSource clinicalAttributeDataSource : clinicalAttributeDataSources) {
            String categoricalKey = clinicalAttributeDataSource.getValue() + ClinicalAttributeDataType.CATEGORICAL;
            String numericKey = clinicalAttributeDataSource.getValue() + ClinicalAttributeDataType.NUMERIC;
            clinicalAttributeNameMap.put(categoricalKey, studyViewRepository.getClinicalDataAttributeNames(clinicalAttributeDataSource, ClinicalAttributeDataType.CATEGORICAL));
            clinicalAttributeNameMap.put(numericKey, studyViewRepository.getClinicalDataAttributeNames(clinicalAttributeDataSource, ClinicalAttributeDataType.NUMERIC));
        }
    }

    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return studyViewRepository.getPatientClinicalData(studyViewFilter, attributeIds, categorizedClinicalDataCountFilter);
    }

    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return studyViewRepository.getSampleClinicalData(studyViewFilter, attributeIds, categorizedClinicalDataCountFilter);
    }


}
