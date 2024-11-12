package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenericAssayDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.treatment.TreatmentCountReportService;
import org.cbioportal.service.util.StudyViewColumnarServiceUtil;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.GenomicDataBinFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.columnar.util.CustomDataFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudyViewColumnarServiceImpl implements StudyViewColumnarService {


    private final StudyViewRepository studyViewRepository;
    private final CustomDataFilterUtil customDataFilterUtil;
    
    private final AlterationCountService alterationCountService;
    private final TreatmentCountReportService treatmentCountReportService;

    @Autowired
    public StudyViewColumnarServiceImpl(StudyViewRepository studyViewRepository, AlterationCountService alterationCountService, TreatmentCountReportService treatmentCountReportService, CustomDataFilterUtil customDataFilterUtil) {
        this.studyViewRepository = studyViewRepository;
        this.alterationCountService = alterationCountService;
        this.treatmentCountReportService = treatmentCountReportService;
        this.customDataFilterUtil = customDataFilterUtil;
    }
    
    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter) {
        
        return studyViewRepository.getFilteredSamples(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return alterationCountService.getMutatedGenes(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getMolecularProfileSampleCounts(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getClinicalEventTypeCounts(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportService.getPatientTreatmentReport(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportService.getSampleTreatmentReport(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<ClinicalDataCountItem> getGenomicDataBinCounts(StudyViewFilter studyViewFilter, List<GenomicDataBinFilter> genomicDataBinFilters) {
        return generateDataCountItemsFromDataCounts(studyViewRepository.getGenomicDataBinCounts(createContext(studyViewFilter), genomicDataBinFilters));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<ClinicalDataCountItem> getGenericAssayDataBinCounts(StudyViewFilter studyViewFilter, List<GenericAssayDataBinFilter> genericAssayDataBinFilters) {
        return generateDataCountItemsFromDataCounts(studyViewRepository.getGenericAssayDataBinCounts(createContext(studyViewFilter), genericAssayDataBinFilters));
    }

    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return alterationCountService.getCnaGenes(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return alterationCountService.getStructuralVariantGenes(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getClinicalAttributeDatatypeMap();
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes) {

        var result = studyViewRepository.getClinicalDataCounts(createContext(studyViewFilter), filteredAttributes);
        
        // fetch the samples by using the provided study view filter
        List<Sample> filteredSamples = studyViewRepository.getFilteredSamples(createContext(studyViewFilter));

        var resultWithAllAttributes = StudyViewColumnarServiceUtil.addClinicalDataCountsForMissingAttributes(result, filteredAttributes, filteredSamples);

        var mergedResult = StudyViewColumnarServiceUtil.mergeClinicalDataCounts(resultWithAllAttributes);
        
//        resultWithAllAttributes.stream().forEach(attr -> {
//            Map<String, List<ClinicalDataCount>> countsPerType = attr.getCounts().stream()
//                .collect(Collectors.groupingBy(ClinicalDataCount::getValue));
//            List<ClinicalDataCount> res = countsPerType.entrySet().stream().map((entry)->{
//                ClinicalDataCount mergedCount = new ClinicalDataCount();
//                mergedCount.setAttributeId(attr.getAttributeId());
//                mergedCount.setValue(entry.getKey());
//                mergedCount.setCount(entry.getValue().stream().mapToInt(ClinicalDataCount::getCount).sum());
//                return mergedCount;
//            }).collect(Collectors.toList());
//            attr.setCounts(res);
//        });
        
        return mergedResult;
    }
    
    

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<CaseListDataCount> getCaseListDataCounts(StudyViewFilter studyViewFilter) {
        // the study view merges case lists by type across studies
        // type is determined by the suffix of case list name (after study name)
        var caseListDataCountsPerStudy = studyViewRepository.getCaseListDataCountsPerStudy(createContext(studyViewFilter));
        return mergeCaseListCounts(caseListDataCountsPerStudy);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        return studyViewRepository.getPatientClinicalData(createContext(studyViewFilter), attributeIds);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        return studyViewRepository.getSampleClinicalData(createContext(studyViewFilter), attributeIds);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<GenomicDataCountItem> getCNACountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        return studyViewRepository.getCNACounts(createContext(studyViewFilter), genomicDataFilters);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<GenericAssayDataCountItem> getGenericAssayDataCounts(StudyViewFilter studyViewFilter, List<GenericAssayDataFilter> genericAssayDataFilters) {
        return studyViewRepository.getGenericAssayDataCounts(createContext(studyViewFilter), genericAssayDataFilters);
    }
    
    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<GenomicDataCountItem> getMutationCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        List<GenomicDataCountItem> genomicDataCountItemList = new ArrayList<>();
        for (GenomicDataFilter genomicDataFilter : genomicDataFilters) {
            Map<String, Integer> counts = studyViewRepository.getMutationCounts(createContext(studyViewFilter), genomicDataFilter);
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

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfiltered(#studyViewFilter)"
    )
    @Override
    public List<GenomicDataCountItem> getMutationTypeCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        return studyViewRepository.getMutationCountsByType(createContext(studyViewFilter), genomicDataFilters);
    }
    
    
    private StudyViewFilterContext createContext(StudyViewFilter studyViewFilter) {
        List<CustomSampleIdentifier> customSampleIdentifiers = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return new StudyViewFilterContext(studyViewFilter, customSampleIdentifiers);
    }
    
    private List<ClinicalDataCountItem> generateDataCountItemsFromDataCounts(List<ClinicalDataCount> dataCounts) {
        return dataCounts.stream().collect(Collectors.groupingBy(ClinicalDataCount::getAttributeId))
            .entrySet().parallelStream().map(e -> {
                ClinicalDataCountItem item = new ClinicalDataCountItem();
                item.setAttributeId(e.getKey());
                item.setCounts(normalizeDataCounts(e.getValue()));
                return item;
            }).toList();
    }

    /**
     * Normalizes data counts by merging attribute values in a case-insensitive way.
     * For example attribute values "TRUE", "True", and 'true' will be merged into a single aggregated count.
     * This method assumes that all the counts in the given dataCounts list has the same attributeId.
     * 
     * @param dataCounts list of data counts for a single attribute
     * 
     * @return normalized list of data counts
     */
    private List<ClinicalDataCount> normalizeDataCounts(List<ClinicalDataCount> dataCounts) {
        Collection<ClinicalDataCount> normalizedDataCounts = dataCounts
            .stream()
            .collect(
                Collectors.groupingBy(
                    c -> c.getValue().toLowerCase(),
                    Collectors.reducing(new ClinicalDataCount(), (count1, count2) -> {
                        // assuming attribute ids are the same for all data counts, just pick the first one
                        String attributeId = 
                            count1.getAttributeId() != null
                                ? count1.getAttributeId() 
                                : count2.getAttributeId();
                        
                        // pick the value in a deterministic way by prioritizing lower case over upper case.
                        // for example, 'True' will be picked in case of 2 different values like 'TRUE', and 'True',
                        // and 'true' will be picked in case of 3 different values like 'TRUE', 'True', and 'true'
                        String value = count1.getValue() != null 
                            ? count1.getValue()
                            : count2.getValue();
                        if (count1.getValue() != null && count2.getValue() != null) {
                            value = count1.getValue().compareTo(count2.getValue()) > 0 
                                ? count1.getValue()
                                : count2.getValue();
                        }
                        
                        // aggregate counts for the merged values 
                        Integer count = (count1.getCount() != null ? count1.getCount(): 0) +
                            (count2.getCount() != null ? count2.getCount(): 0);
                        
                        ClinicalDataCount aggregated = new ClinicalDataCount();
                        aggregated.setAttributeId(attributeId);
                        aggregated.setValue(value);
                        aggregated.setCount(count);
                        return aggregated;
                    })
                )
            )
            .values();
        
        return new ArrayList<>(normalizedDataCounts);
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
