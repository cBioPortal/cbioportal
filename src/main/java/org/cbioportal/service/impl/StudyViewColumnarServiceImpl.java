package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.StudyViewColumnarService;
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

    @Autowired
    public StudyViewColumnarServiceImpl(StudyViewRepository studyViewRepository, AlterationCountService alterationCountService) {
        this.studyViewRepository = studyViewRepository;
        this.alterationCountService = alterationCountService;
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
    public List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getGenomicDataCounts(studyViewFilter);
    }
    
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter) {
        return alterationCountService.getCnaGenes(studyViewFilter);
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) {
        return alterationCountService.getStructuralVariantGenes(studyViewFilter);
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
        List<GenomicDataCountItem> genomicDataCountItemList = new ArrayList<>();
        for (GenomicDataFilter genomicDataFilter : genomicDataFilters) {
            Map<String, Integer> counts = studyViewRepository.getCNACounts(studyViewFilter, genomicDataFilter);
            List<GenomicDataCount> genomicDataCountList = new ArrayList<>();
            if (counts.getOrDefault("amplifiedCount", 0) > 0)
                genomicDataCountList.add(new GenomicDataCount("Amplified", "2", counts.get("amplifiedCount")));
            if (counts.getOrDefault("gainedCount", 0) > 0)
                genomicDataCountList.add(new GenomicDataCount("Gained", "1", counts.get("gainedCount")));
            if (counts.getOrDefault("diploidCount", 0) > 0)
                genomicDataCountList.add(new GenomicDataCount("Diploid", "0", counts.get("diploidCount")));
            if (counts.getOrDefault("heterozygouslyDeletedCount", 0) > 0)
                genomicDataCountList.add(new GenomicDataCount("Heterozygously Deleted", "-1", counts.get("heterozygouslyDeletedCount")));
            if (counts.getOrDefault("homozygouslyDeletedCount", 0) > 0)
                genomicDataCountList.add(new GenomicDataCount("Homozygously Deleted", "-2", counts.get("homozygouslyDeletedCount")));
            if (counts.getOrDefault("amplifiedCount", 0) > 0)
                genomicDataCountList.add(new GenomicDataCount("NA", "NA", counts.get("NACount")));
            genomicDataCountItemList.add(new GenomicDataCountItem(genomicDataFilter.getHugoGeneSymbol(), "cna", genomicDataCountList));
        }
        return genomicDataCountItemList;
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
