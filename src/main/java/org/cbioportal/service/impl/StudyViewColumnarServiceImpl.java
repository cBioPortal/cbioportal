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
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.treatment.TreatmentCountReportService;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.columnar.util.CustomDataFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
        List<SampleIdentifier> customDataSamples = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return studyViewRepository.getFilteredSamples(studyViewFilter, customDataSamples);
    }

    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) {
        List<SampleIdentifier> customDataSamples = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return alterationCountService.getMutatedGenes(studyViewFilter, customDataSamples);
    }

    @Override
    public List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter) {
        List<SampleIdentifier> customDataSamples = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return studyViewRepository.getGenomicDataCounts(studyViewFilter, customDataSamples);
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
        List<SampleIdentifier> customDataSamples = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return alterationCountService.getCnaGenes(studyViewFilter, customDataSamples);
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) {
        List<SampleIdentifier> customDataSamples = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return alterationCountService.getStructuralVariantGenes(studyViewFilter, customDataSamples);
    }

    @Override
    public Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap() {
        return studyViewRepository.getClinicalAttributeDatatypeMap();
    }
    
    @Override
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes) {
        List<SampleIdentifier> customDataSamples = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return studyViewRepository.getClinicalDataCounts(studyViewFilter, filteredAttributes, customDataSamples)
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
        List<SampleIdentifier> customDataSamples = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return studyViewRepository.getCaseListDataCounts(studyViewFilter, customDataSamples);
    }
    
    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        List<SampleIdentifier> customDataSamples = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return studyViewRepository.getPatientClinicalData(studyViewFilter, attributeIds, customDataSamples);
    }

    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        List<SampleIdentifier> customDataSamples = customDataFilterUtil.extractCustomDataSamples(studyViewFilter);
        return studyViewRepository.getSampleClinicalData(studyViewFilter, attributeIds, customDataSamples);
    }
}
