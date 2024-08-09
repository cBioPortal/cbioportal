package org.cbioportal.persistence.mybatisclickhouse;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.PatientTreatment;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatment;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class StudyViewMyBatisRepository implements StudyViewRepository {

    private Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributesMap = new HashMap<>();


    private static final List<String> FILTERED_CLINICAL_ATTR_VALUES = Collections.emptyList();
    private final StudyViewMapper mapper;
   
    @Autowired
    public StudyViewMyBatisRepository(StudyViewMapper mapper) {
        this.mapper = mapper;    
    }
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getFilteredSamples(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter));
    }
    
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getMutatedGenes(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    }

    @Override
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getCnaGenes(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getStructuralVariantGenes(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    }

    @Override
    public List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getClinicalDataCounts(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter),
            filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES );
    }

    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        var sampleCounts = mapper.getMolecularProfileSampleCounts(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter));

        Map<String, List<GenomicDataCount>> countsPerType = sampleCounts.stream()
            .collect((Collectors.groupingBy(GenomicDataCount::getValue)));

        // different cancer studies combined into one cohort will have separate molecular profiles
        // of a given type (e.g. mutation).  We need to merge the counts for these
        // different profiles based on the type and choose a label
        // this code just picks the first label, which assumes that the labels will match
        // across studies. 
        List<GenomicDataCount> mergedCounts = new ArrayList<>();
        for (Map.Entry<String,List<GenomicDataCount>> entry : countsPerType.entrySet()) {
            var dc = new GenomicDataCount();
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
    
    @Override
    public List<ClinicalAttribute> getClinicalAttributes() {
        return mapper.getClinicalAttributes();
    }

    @Override
    public Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap() {
        if (clinicalAttributesMap.isEmpty()) {
            buildClinicalAttributeNameMap();
        }
        
        Map<String, ClinicalDataType> attributeDatatypeMap = new HashMap<>();

        clinicalAttributesMap
            .get(ClinicalAttributeDataSource.SAMPLE)
            .forEach(attribute -> attributeDatatypeMap.put(attribute.getAttrId(), ClinicalDataType.SAMPLE));

        clinicalAttributesMap
            .get(ClinicalAttributeDataSource.PATIENT)
            .forEach(attribute -> attributeDatatypeMap.put(attribute.getAttrId(), ClinicalDataType.PATIENT));
        
        return attributeDatatypeMap;
    }

    @Override
    public List<CaseListDataCount> getCaseListDataCounts(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getCaseListDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter));
    }


    private boolean shouldApplyPatientIdFilters(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return studyViewFilter.getClinicalEventFilters() != null && !studyViewFilter.getClinicalEventFilters().isEmpty()
            || studyViewFilter.getPatientTreatmentFilters() != null && studyViewFilter.getPatientTreatmentFilters().getFilters()!= null && !studyViewFilter.getPatientTreatmentFilters().getFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters().isEmpty();
    }

    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getSampleClinicalDataFromStudyViewFilter(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter), attributeIds);
    }
    
    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getPatientClinicalDataFromStudyViewFilter(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter), attributeIds);
    }
    
    @Override
    public Map<String, Integer> getTotalProfiledCounts(StudyViewFilter studyViewFilter, String alterationType) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getTotalProfiledCounts(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter), alterationType)
            .stream()
            .collect(Collectors.groupingBy(AlterationCountByGene::getHugoGeneSymbol,
                Collectors.mapping(AlterationCountByGene::getNumberOfProfiledCases, Collectors.summingInt(Integer::intValue))));
    }

    @Override
    public int getFilteredSamplesCount(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getFilteredSamplesCount(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter));
    }

    @Override
    public Map<String, Set<String>> getMatchingGenePanelIds(StudyViewFilter studyViewFilter, String alterationType) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getMatchingGenePanelIds(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter), alterationType)
            .stream()
            .collect(Collectors.groupingBy(GenePanelToGene::getHugoGeneSymbol,
                Collectors.mapping(GenePanelToGene::getGenePanelId, Collectors.toSet())));
    }

    @Override
    public int getTotalProfiledCountsByAlterationType(StudyViewFilter studyViewFilter, String alterationType) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
       return mapper.getTotalProfiledCountByAlterationType(studyViewFilter, categorizedClinicalDataCountFilter,
           shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter), alterationType); 
    }

    @Override
    public int getSampleProfileCountWithoutPanelData(StudyViewFilter studyViewFilter, String alterationType) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getSampleProfileCountWithoutPanelData(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter), alterationType);
    }


    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getClinicalEventTypeCounts(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter,categorizedClinicalDataCountFilter));
    }

    @Override
    public List<PatientTreatment> getPatientTreatments(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getPatientTreatments(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(studyViewFilter, categorizedClinicalDataCountFilter));
    }
    
    @Override
    public int getTotalPatientTreatmentCount(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
       return mapper.getPatientTreatmentCounts(studyViewFilter, categorizedClinicalDataCountFilter, 
            shouldApplyPatientIdFilters(studyViewFilter, categorizedClinicalDataCountFilter));
    }

    @Override
    public List<SampleTreatment> getSampleTreatments(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getSampleTreatmentCounts(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter, categorizedClinicalDataCountFilter));
    }

    @Override
    public int getTotalSampleTreatmentCount(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getTotalSampleTreatmentCounts(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(studyViewFilter, categorizedClinicalDataCountFilter));
    }

    private void buildClinicalAttributeNameMap() {
        clinicalAttributesMap = this.getClinicalAttributes()
            .stream()
            .collect(Collectors.groupingBy(ca -> ca.getPatientAttribute() ? ClinicalAttributeDataSource.PATIENT : ClinicalAttributeDataSource.SAMPLE));
    }
    
    private Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> getClinicalAttributeNameMap() {
        if (clinicalAttributesMap.isEmpty()) {
            buildClinicalAttributeNameMap();
        }
        return clinicalAttributesMap;
    }
    
    private CategorizedClinicalDataCountFilter extractClinicalDataCountFilters(final StudyViewFilter studyViewFilter) {
        if (clinicalAttributesMap.isEmpty()) {
            buildClinicalAttributeNameMap();
        }

        if (studyViewFilter.getClinicalDataFilters() == null) {
            return CategorizedClinicalDataCountFilter.getBuilder().build();
        }
        
        List<String> patientCategoricalAttributes = clinicalAttributesMap.get(ClinicalAttributeDataSource.PATIENT)
            .stream().filter(ca -> ca.getDatatype().equals("STRING"))
            .map(ClinicalAttribute::getAttrId)
            .toList();

        List<String> patientNumericalAttributes = clinicalAttributesMap.get(ClinicalAttributeDataSource.PATIENT)
            .stream().filter(ca -> ca.getDatatype().equals("NUMBER"))
            .map(ClinicalAttribute::getAttrId)
            .toList();

        List<String> sampleCategoricalAttributes = clinicalAttributesMap.get(ClinicalAttributeDataSource.SAMPLE)
            .stream().filter(ca -> ca.getDatatype().equals("STRING"))
            .map(ClinicalAttribute::getAttrId)
            .toList();

        List<String> sampleNumericalAttributes = clinicalAttributesMap.get(ClinicalAttributeDataSource.SAMPLE)
            .stream().filter(ca -> ca.getDatatype().equals("NUMBER"))
            .map(ClinicalAttribute::getAttrId)
            .toList();
        
        return CategorizedClinicalDataCountFilter.getBuilder()
            .setPatientCategoricalClinicalDataFilters(studyViewFilter.getClinicalDataFilters()
                .stream().filter(clinicalDataFilter -> patientCategoricalAttributes.contains(clinicalDataFilter.getAttributeId()))
                .collect(Collectors.toList()))
            .setPatientNumericalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                .filter(clinicalDataFilter -> patientNumericalAttributes.contains(clinicalDataFilter.getAttributeId()))
                .collect(Collectors.toList()))
            .setSampleCategoricalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                .filter(clinicalDataFilter -> sampleCategoricalAttributes.contains(clinicalDataFilter.getAttributeId()))
                .collect(Collectors.toList()))
            .setSampleNumericalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                .filter(clinicalDataFilter -> sampleNumericalAttributes.contains(clinicalDataFilter.getAttributeId()))
                .collect(Collectors.toList()))
            .build();
    }
    

}