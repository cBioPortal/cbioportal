package org.cbioportal.service.impl;

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
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.treatment.TreatmentCountReportService;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.columnar.util.CustomDataFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

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

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter) {
        
        return studyViewRepository.getFilteredSamples(createContext(studyViewFilter));
    }

    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) {
        return alterationCountService.getMutatedGenes(createContext(studyViewFilter));
    }

    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getMolecularProfileSampleCounts(createContext(studyViewFilter));
    }

    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getClinicalEventTypeCounts(createContext(studyViewFilter));
    }

    @Override
    public PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportService.getPatientTreatmentReport(createContext(studyViewFilter));
    }

    @Override
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter) {
        return treatmentCountReportService.getSampleTreatmentReport(createContext(studyViewFilter));
    }

    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter) {
        return alterationCountService.getCnaGenes(createContext(studyViewFilter));
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) {
        return alterationCountService.getStructuralVariantGenes(createContext(studyViewFilter));
    }

    @Override
    public Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap() {
        return studyViewRepository.getClinicalAttributeDatatypeMap();
    }
    
    @Override
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes) {
        return studyViewRepository.getClinicalDataCounts(createContext(studyViewFilter), filteredAttributes)
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
        var caseListDataCountsPerStudy = studyViewRepository.getCaseListDataCountsPerStudy(createContext(studyViewFilter));
        return mergeCaseListCounts(caseListDataCountsPerStudy);
    }
    
    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        return studyViewRepository.getPatientClinicalData(createContext(studyViewFilter), attributeIds);
    }

    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        return studyViewRepository.getSampleClinicalData(createContext(studyViewFilter), attributeIds);
    }

    @Override
    public List<GenomicDataCountItem> getCNACountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        return studyViewRepository.getCNACounts(createContext(studyViewFilter), genomicDataFilters);
    }
    
    @Override
    public List<GenomicDataCountItem> getMutationCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        List<GenomicDataCountItem> genomicDataCountItemList = new ArrayList<>();
        int totalCount = studyViewRepository.getFilteredSamplesCount(createContext(studyViewFilter));
        List<AlterationCountByGene> alterationCountByGenes = alterationCountService.getMutatedGenes(createContext(studyViewFilter));
        for (GenomicDataFilter genomicDataFilter : genomicDataFilters) {
            AlterationCountByGene filteredAlterationCount = alterationCountByGenes.stream()
                .filter(g -> g.getHugoGeneSymbol().equals(genomicDataFilter.getHugoGeneSymbol()))
                .findFirst()
                .orElse(new AlterationCountByGene());
            int mutatedCount = filteredAlterationCount.getNumberOfAlteredCases();
            int profiledCount = filteredAlterationCount.getNumberOfProfiledCases();
            List<GenomicDataCount> genomicDataCountList = new ArrayList<>();
            genomicDataCountList.add(new GenomicDataCount("Mutated", "MUTATED", mutatedCount, mutatedCount));
            genomicDataCountList.add(new GenomicDataCount("Not Mutated", "NOT_MUTATED", profiledCount - mutatedCount, profiledCount - mutatedCount));
            genomicDataCountList.add(new GenomicDataCount("Not Profiled", "NOT_PROFILED", totalCount - profiledCount, totalCount - profiledCount));
            genomicDataCountItemList.add(new GenomicDataCountItem(genomicDataFilter.getHugoGeneSymbol(), "mutations", genomicDataCountList));
        }
        return genomicDataCountItemList;
    }

    @Override
    public List<GenomicDataCountItem> getMutationTypeCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        return studyViewRepository.getMutationCountsByType(createContext(studyViewFilter), genomicDataFilters);
    }
    
    private StudyViewFilterContext createContext(StudyViewFilter studyViewFilter) {
        List<CustomSampleIdentifier> customSampleIdentifiers = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return new StudyViewFilterContext(studyViewFilter, customSampleIdentifiers);
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
