package org.cbioportal.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import com.google.common.collect.Range;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.treatment.TreatmentCountReportService;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.CustomDatatype;
import org.cbioportal.web.util.DataBinHelper;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class StudyViewColumnarServiceImpl implements StudyViewColumnarService {


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
    
    private final AlterationCountService alterationCountService;
    private final TreatmentCountReportService treatmentCountReportService;

    @Autowired
    public StudyViewColumnarServiceImpl(StudyViewRepository studyViewRepository, AlterationCountService alterationCountService, TreatmentCountReportService treatmentCountReportService, SampleService sampleService, StudyViewFilterUtil studyViewFilterUtil, CustomDataService customDataService, DataBinHelper dataBinHelper) {
        this.studyViewRepository = studyViewRepository;
        this.alterationCountService = alterationCountService;
        this.treatmentCountReportService = treatmentCountReportService;
        this.sampleService = sampleService;
        this.studyViewFilterUtil = studyViewFilterUtil;
        this.customDataService = customDataService;
        this.dataBinHelper = dataBinHelper;
    }

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter) {
        List<SampleIdentifier> customDataSamples = extractCustomDataSamples(studyViewFilter);
        return studyViewRepository.getFilteredSamples(studyViewFilter, customDataSamples);
    }

    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) {
        return alterationCountService.getMutatedGenes(studyViewFilter);
    }

    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getMolecularProfileSampleCounts(studyViewFilter);
    }

    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getClinicalEventTypeCounts(studyViewFilter);
    }

    @Override
    public PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportService.getPatientTreatmentReport(studyViewFilter);
    }

    @Override
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportService.getSampleTreatmentReport(studyViewFilter);
    }

    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter) {
        return alterationCountService.getCnaGenes(studyViewFilter);
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) {
        return alterationCountService.getStructuralVariantGenes(studyViewFilter);
    }

    @Override
    public Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap() {
        return studyViewRepository.getClinicalAttributeDatatypeMap();
    }
    
    @Override
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes) {
        return studyViewRepository.getClinicalDataCounts(studyViewFilter, filteredAttributes)
            .stream().collect(Collectors.groupingBy(ClinicalDataCount::getAttributeId))
            .entrySet().parallelStream().map(e -> {
                ClinicalDataCountItem item = new ClinicalDataCountItem();
                item.setAttributeId(e.getKey());
                item.setCounts(e.getValue());
                return item;
            }).collect(Collectors.toList());
    }
    
    @Override
    public List<CaseListDataCount> getCaseListDataCounts(StudyViewFilter studyViewFilter) {
        // the study view merges case lists by type across studies
        // type is determined by the suffix of case list name (after study name)
        var caseListDataCountsPerStudy = studyViewRepository.getCaseListDataCountsPerStudy(studyViewFilter);
        return mergeCaseListCounts(caseListDataCountsPerStudy);
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

    
    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        return studyViewRepository.getPatientClinicalData(studyViewFilter, attributeIds);
    }

    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        return studyViewRepository.getSampleClinicalData(studyViewFilter, attributeIds);
    }

    @Override
    public List<GenomicDataCountItem> getCNACountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        return studyViewRepository.getCNACounts(studyViewFilter, genomicDataFilters);
    }
    
    @Override
    public List<GenomicDataCountItem> getMutationCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        List<GenomicDataCountItem> genomicDataCountItemList = new ArrayList<>();
        for (GenomicDataFilter genomicDataFilter : genomicDataFilters) {
            Map<String, Integer> counts = studyViewRepository.getMutationCounts(studyViewFilter, genomicDataFilter);
            List<GenomicDataCount> genomicDataCountList = new ArrayList<>();
            if (counts.getOrDefault("mutatedCount", 0) > 0)
                genomicDataCountList.add(new GenomicDataCount("Mutated", "MUTATED", counts.get("mutatedCount"), counts.get("mutatedCount")));
            if (counts.getOrDefault("notMutatedCount", 0) > 0)
                genomicDataCountList.add(new GenomicDataCount("Not Mutated", "NOT_MUTATED", counts.get("notMutatedCount"), counts.get("notMutatedCount")));
            if (counts.getOrDefault("notProfiledCount", 0) > 0)
                genomicDataCountList.add(new GenomicDataCount("Not Profiled", "NOT_PROFILED", counts.get("notProfiledCount"), counts.get("notProfiledCount")));
            genomicDataCountItemList.add(new GenomicDataCountItem(genomicDataFilter.getHugoGeneSymbol(), "mutations", genomicDataCountList));
        }
        return genomicDataCountItemList;
    }

    @Override
    public List<GenomicDataCountItem> getMutationTypeCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        return studyViewRepository.getMutationCountsByType(studyViewFilter, genomicDataFilters);
    }



    public static List<CaseListDataCount> mergeCaseListCounts(List<CaseListDataCount> counts) {
        Map<String, List<CaseListDataCount>> countsPerListType = counts.stream()
            .collect((Collectors.groupingBy(CaseListDataCount::getValue)));

        // different cancer studies combined into one cohort will have separate case lists
        // of a given type (e.g. rppa).  We need to merge the counts for these
        // different lists based on the type and choose a label
        // this code just picks the first label, which assumes that the labels will match for a give type
        List<CaseListDataCount> mergedCounts = new ArrayList<>();
        for (Map.Entry<String,List<CaseListDataCount>> entry : countsPerListType.entrySet()) {
            var dc = new CaseListDataCount();
            dc.setValue(entry.getKey());
            // here just snatch the label of the first profile
            dc.setLabel(entry.getValue().get(0).getLabel());
            Integer sum = entry.getValue().stream()
                .map(x -> x.getCount())
                .collect(Collectors.summingInt(Integer::intValue));
            dc.setCount(sum);
            mergedCounts.add(dc);
        }
        return mergedCounts;
    }



}
