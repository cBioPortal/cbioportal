package org.cbioportal.persistence.mybatisclickhouse;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
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
        return mapper.getFilteredSamples(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter));
    }
    
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getMutatedGenes(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    }

    @Override
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getCnaGenes(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getStructuralVariantGenes(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    }

    @Override
    public List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getClinicalDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
            filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES );
    }

    @Override
    public List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getGenomicDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter));
    }
    
    @Override
    public List<ClinicalAttribute> getClinicalAttributes() {
        return mapper.getClinicalAttributes();
    }

    @Override
    public List<CaseListDataCount> getCaseListDataCounts(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getCaseListDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter));
    }


    private boolean shouldApplyPatientIdFilters(CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters().isEmpty();
    }

    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getSampleClinicalDataFromStudyViewFilter(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), attributeIds);
    }
    
    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getPatientClinicalDataFromStudyViewFilter(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), attributeIds);
    }
    
    @Override
    public Map<String, AlterationCountByGene> getTotalProfiledCounts(StudyViewFilter studyViewFilter, String alterationType) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getTotalProfiledCounts(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), alterationType);
    }

    @Override
    public int getFilteredSamplesCount(StudyViewFilter studyViewFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getFilteredSamplesCount(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter));
    }

    @Override
    public Map<String, Set<String>> getMatchingGenePanelIds(StudyViewFilter studyViewFilter, String alterationType) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getMatchingGenePanelIds(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), alterationType)
            .stream()
            .collect(Collectors.groupingBy(GenePanelToGene::getHugoGeneSymbol,
                Collectors.mapping(GenePanelToGene::getGenePanelId, Collectors.toSet())));
    }

    @Override
    public int getTotalProfiledCountsByAlterationType(StudyViewFilter studyViewFilter, String alterationType) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
       return mapper.getTotalProfiledCountByAlterationType(studyViewFilter, categorizedClinicalDataCountFilter,
           shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), alterationType); 
    }

    private void buildClinicalAttributeNameMap() {
        clinicalAttributesMap = this.getClinicalAttributes()
            .stream()
            .collect(Collectors.groupingBy(ca -> ca.getPatientAttribute() ? ClinicalAttributeDataSource.PATIENT : ClinicalAttributeDataSource.SAMPLE));
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
    
    @Override
    public List<GenomicDataCountItem> getCNACounts(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getCNACounts(studyViewFilter, categorizedClinicalDataCountFilter, genomicDataFilters);
    }

    public Map<String, Integer> getMutationCounts(StudyViewFilter studyViewFilter, GenomicDataFilter genomicDataFilter) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getMutationCounts(studyViewFilter, categorizedClinicalDataCountFilter, genomicDataFilter);
    }
    
    public List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters) {
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter);
        return mapper.getMutationCountsByType(studyViewFilter, categorizedClinicalDataCountFilter, genomicDataFilters);
    }

}