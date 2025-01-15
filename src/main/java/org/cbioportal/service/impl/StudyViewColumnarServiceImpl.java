package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalAttribute;
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
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.alteration.AlterationCountByGeneService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.treatment.TreatmentCountReportService;
import org.cbioportal.service.util.StudyViewColumnarServiceUtil;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
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

import static org.cbioportal.web.columnar.util.ClinicalDataXyPlotUtil.combineClinicalDataForXyPlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class StudyViewColumnarServiceImpl implements StudyViewColumnarService {


    private final StudyViewRepository studyViewRepository;
    private final CustomDataFilterUtil customDataFilterUtil;
    
    private final AlterationCountByGeneService alterationCountByGeneService;
    private final TreatmentCountReportService treatmentCountReportService;

    @Autowired
    public StudyViewColumnarServiceImpl(StudyViewRepository studyViewRepository, AlterationCountByGeneService alterationCountByGeneService, TreatmentCountReportService treatmentCountReportService, CustomDataFilterUtil customDataFilterUtil) {
        this.studyViewRepository = studyViewRepository;
        this.alterationCountByGeneService = alterationCountByGeneService;
        this.treatmentCountReportService = treatmentCountReportService;
        this.customDataFilterUtil = customDataFilterUtil;
    }
    
    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter) {
        
        return studyViewRepository.getFilteredSamples(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return alterationCountByGeneService.getMutatedGenes(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getMolecularProfileSampleCounts(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getClinicalEventTypeCounts(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportService.getPatientTreatmentReport(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportService.getSampleTreatmentReport(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<ClinicalDataCountItem> getGenomicDataBinCounts(StudyViewFilter studyViewFilter, List<GenomicDataBinFilter> genomicDataBinFilters) {
        return generateDataCountItemsFromDataCounts(studyViewRepository.getGenomicDataBinCounts(createContext(studyViewFilter), genomicDataBinFilters));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<ClinicalDataCountItem> getGenericAssayDataBinCounts(StudyViewFilter studyViewFilter, List<GenericAssayDataBinFilter> genericAssayDataBinFilters) {
        return generateDataCountItemsFromDataCounts(studyViewRepository.getGenericAssayDataBinCounts(createContext(studyViewFilter), genericAssayDataBinFilters));
    }

    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return alterationCountByGeneService.getCnaGenes(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) throws StudyNotFoundException {
        return alterationCountByGeneService.getStructuralVariantGenes(createContext(studyViewFilter));
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getClinicalAttributeDatatypeMap();
    }
    
    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes) {

        var context = createContext(studyViewFilter);

        List<String> involvedCancerStudies = context.involvedCancerStudies();

        var result = studyViewRepository.getClinicalDataCounts(context, filteredAttributes);
        
        // normalize data counts so that values like TRUE, True, and true are all merged in one count
        result.forEach(item -> item.setCounts(StudyViewColumnarServiceUtil.normalizeDataCounts(item.getCounts())));
        
        // attributes may be missing in result set because they have been filtered out
        // e.g. if the filtered samples happen to have no SEX data, they will not appear in the list
        // even though the inferred value of those attributes is NA
        // the following code restores these counts for missing attributes
        if (result.size() != filteredAttributes.size()) {
            var attributes = getClinicalAttributesForStudies(involvedCancerStudies)
                .stream()
                .filter(attribute -> filteredAttributes.contains(attribute.getAttrId()))
                .toList();

            Integer filteredSampleCount = studyViewRepository.getFilteredSamplesCount(createContext(studyViewFilter));
            Integer filteredPatientCount = studyViewRepository.getFilteredPatientCount(createContext(studyViewFilter));
    
            result = StudyViewColumnarServiceUtil.addClinicalDataCountsForMissingAttributes(
                result,
                attributes,
                filteredSampleCount,
                filteredPatientCount
            );
        }
        
        return StudyViewColumnarServiceUtil.mergeClinicalDataCounts(result);
        
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse()"
    )
    public List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds) {
        return studyViewRepository.getClinicalAttributesForStudies(studyIds).stream().toList();
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<CaseListDataCount> getCaseListDataCounts(StudyViewFilter studyViewFilter) {
        // the study view merges case lists by type across studies
        // type is determined by the suffix of case list name (after study name)
        var caseListDataCountsPerStudy = studyViewRepository.getCaseListDataCountsPerStudy(createContext(studyViewFilter));
        return StudyViewColumnarServiceUtil.mergeCaseListCounts(caseListDataCountsPerStudy);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        return studyViewRepository.getPatientClinicalData(createContext(studyViewFilter), attributeIds);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        return studyViewRepository.getSampleClinicalData(createContext(studyViewFilter), attributeIds);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<GenomicDataCountItem> getCNACountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        return studyViewRepository.getCNACounts(createContext(studyViewFilter), genomicDataFilters);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<GenericAssayDataCountItem> getGenericAssayDataCounts(StudyViewFilter studyViewFilter, List<GenericAssayDataFilter> genericAssayDataFilters) {
        return studyViewRepository.getGenericAssayDataCounts(createContext(studyViewFilter), genericAssayDataFilters);
    }
    
    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<GenomicDataCountItem> getMutationCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        List<GenomicDataCountItem> genomicDataCountItemList = new ArrayList<>();
        for (GenomicDataFilter genomicDataFilter : genomicDataFilters) {
            Map<String, Integer> counts = studyViewRepository.getMutationCounts(createContext(studyViewFilter), genomicDataFilter);
            genomicDataCountItemList.add(StudyViewColumnarServiceUtil.createGenomicDataCountItemFromMutationCounts(genomicDataFilter, counts));
        }
        return genomicDataCountItemList;
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<GenomicDataCountItem> getMutationTypeCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        return studyViewRepository.getMutationCountsByType(createContext(studyViewFilter), genomicDataFilters);
    }
    
    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public List<ClinicalData> fetchClinicalDataForXyPlot(
        StudyViewFilter studyViewFilter,
        List<String> attributeIds,
        boolean shouldFilterNonEmptyClinicalData
    ) {
        List<ClinicalData> sampleClinicalDataList = this.getSampleClinicalData(studyViewFilter, attributeIds);
        List<ClinicalData> patientClinicalDataList = this.getPatientClinicalData(studyViewFilter, attributeIds);
        List<Sample> samples = Collections.emptyList();

        if (!patientClinicalDataList.isEmpty()) {
            // fetch samples for the given study view filter.
            // we need this to construct the complete patient to sample map. 
            samples = this.getFilteredSamples(studyViewFilter);
        }

        return combineClinicalDataForXyPlot(
            sampleClinicalDataList,
            patientClinicalDataList,
            samples,
            shouldFilterNonEmptyClinicalData
        );
    }
    
    private StudyViewFilterContext createContext(StudyViewFilter studyViewFilter) {
        List<CustomSampleIdentifier> customSampleIdentifiers = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        List<String> involvedCancerStudies = customDataFilterUtil.extractInvolvedCancerStudies(studyViewFilter);
        return new StudyViewFilterContext(studyViewFilter, customSampleIdentifiers, involvedCancerStudies);
    }
    
    private List<ClinicalDataCountItem> generateDataCountItemsFromDataCounts(List<ClinicalDataCount> dataCounts) {
        return dataCounts.stream().collect(Collectors.groupingBy(ClinicalDataCount::getAttributeId))
            .entrySet().parallelStream().map(e -> {
                ClinicalDataCountItem item = new ClinicalDataCountItem();
                item.setAttributeId(e.getKey());
                item.setCounts(StudyViewColumnarServiceUtil.normalizeDataCounts(e.getValue()));
                return item;
            }).toList();
    }



}
