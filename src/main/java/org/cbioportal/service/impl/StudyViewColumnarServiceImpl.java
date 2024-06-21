package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.enums.ClinicalAttributeDataType;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudyViewColumnarServiceImpl implements StudyViewColumnarService {

    private final Map<String, List<String>> clinicalAttributeNameMap = new HashMap<>();


    private final StudyViewRepository studyViewRepository;
    
    private final AlterationCountService alterationCountService;

    @Autowired
    public StudyViewColumnarServiceImpl(StudyViewRepository studyViewRepository, AlterationCountService alterationCountService) {
        this.studyViewRepository = studyViewRepository;
        this.alterationCountService = alterationCountService;
    }

    @Override
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return studyViewRepository.getFilteredSamples(studyViewFilter, categorizedClinicalDataCountFilter);
    }

    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return alterationCountService.getMutatedGenes(studyViewFilter, categorizedClinicalDataCountFilter);
    }

    @Override
    public List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return studyViewRepository.getGenomicDataCounts(studyViewFilter, categorizedClinicalDataCountFilter);
    }
    
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return alterationCountService.getCnaGenes(studyViewFilter, categorizedClinicalDataCountFilter);
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return alterationCountService.getStructuralVariantGenes(studyViewFilter, categorizedClinicalDataCountFilter);
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

    private CategorizedClinicalDataCountFilter extractClinicalDataCountFilters(final StudyViewFilter studyViewFilter) {
        if (clinicalAttributeNameMap.isEmpty()) {
            buildClinicalAttributeNameMap();
        }

        if (studyViewFilter.getClinicalDataFilters() == null) {
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
        for (ClinicalAttributeDataSource clinicalAttributeDataSource : clinicalAttributeDataSources) {
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
