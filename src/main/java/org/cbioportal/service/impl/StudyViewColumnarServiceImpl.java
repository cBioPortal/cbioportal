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
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.treatment.TreatmentCountReportService;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudyViewColumnarServiceImpl implements StudyViewColumnarService {


    private final StudyViewRepository studyViewRepository;
    
    private final AlterationCountService alterationCountService;
    private final TreatmentCountReportService treatmentCountReportService;

    @Autowired
    public StudyViewColumnarServiceImpl(StudyViewRepository studyViewRepository, 
                                        AlterationCountService alterationCountService,
                                        TreatmentCountReportService treatmentCountReportService) {
        this.studyViewRepository = studyViewRepository;
        this.alterationCountService = alterationCountService;
        this.treatmentCountReportService = treatmentCountReportService;
    }

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getFilteredSamples(studyViewFilter);
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
        return studyViewRepository.getCaseListDataCounts(studyViewFilter);
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

}
