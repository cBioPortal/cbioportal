package org.cbioportal.persistence.mybatisclickhouse;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.PatientTreatment;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatment;
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.GenomicDataFilter;
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
    public List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredSamples(createStudyViewFilterHelper(studyViewFilterContext));
    }
    
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getMutatedGenes(createStudyViewFilterHelper(studyViewFilterContext),
            AlterationFilterHelper.build(studyViewFilterContext.studyViewFilter().getAlterationFilter()), null);
    }

    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext, List<String> hugoGeneSymbols) {
        return mapper.getMutatedGenes(createStudyViewFilterHelper(studyViewFilterContext),
            AlterationFilterHelper.build(studyViewFilterContext.studyViewFilter().getAlterationFilter()), hugoGeneSymbols);
    }

    @Override
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getCnaGenes(createStudyViewFilterHelper(studyViewFilterContext),
            AlterationFilterHelper.build(studyViewFilterContext.studyViewFilter().getAlterationFilter()));
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getStructuralVariantGenes(createStudyViewFilterHelper(studyViewFilterContext),
            AlterationFilterHelper.build(studyViewFilterContext.studyViewFilter().getAlterationFilter()));
    }

    @Override
    public List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
        return mapper.getClinicalDataCounts(createStudyViewFilterHelper(studyViewFilterContext),
            filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES);
    }

    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilterContext studyViewFilterContext) {
        var sampleCounts = mapper.getMolecularProfileSampleCounts(createStudyViewFilterHelper(studyViewFilterContext));
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
    
    public StudyViewFilterHelper createStudyViewFilterHelper(StudyViewFilterContext studyViewFilterContext) {
        return StudyViewFilterHelper.build(studyViewFilterContext.studyViewFilter(), studyViewFilterContext.customDataFilterSamples());    
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
    public List<CaseListDataCount> getCaseListDataCountsPerStudy(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getCaseListDataCountsPerStudy(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds) {
        return mapper.getSampleClinicalDataFromStudyViewFilter(createStudyViewFilterHelper(studyViewFilterContext), attributeIds);
    }
    
    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds) {
        return mapper.getPatientClinicalDataFromStudyViewFilter(createStudyViewFilterHelper(studyViewFilterContext), attributeIds);
    }

    @Override
    public Map<String, Integer> getTotalProfiledCounts(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getTotalProfiledCounts(createStudyViewFilterHelper(studyViewFilterContext), alterationType, null)
            .stream()
            .collect(Collectors.groupingBy(AlterationCountByGene::getHugoGeneSymbol,
                Collectors.mapping(AlterationCountByGene::getNumberOfProfiledCases, Collectors.summingInt(Integer::intValue))));
    }
    
    @Override
    public Map<String, Integer> getTotalProfiledCounts(StudyViewFilterContext studyViewFilterContext, String alterationType, List<String> hugoGeneSymbols) {
        return mapper.getTotalProfiledCounts(createStudyViewFilterHelper(studyViewFilterContext), alterationType, hugoGeneSymbols)
            .stream()
            .collect(Collectors.groupingBy(AlterationCountByGene::getHugoGeneSymbol,
                Collectors.mapping(AlterationCountByGene::getNumberOfProfiledCases, Collectors.summingInt(Integer::intValue))));
    }

    @Override
    public int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredSamplesCount(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public Map<String, Set<String>> getMatchingGenePanelIds(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getMatchingGenePanelIds(createStudyViewFilterHelper(studyViewFilterContext), alterationType, null)
            .stream()
            .collect(Collectors.groupingBy(GenePanelToGene::getHugoGeneSymbol,
                Collectors.mapping(GenePanelToGene::getGenePanelId, Collectors.toSet())));
    }

    @Override
    public Map<String, Set<String>> getMatchingGenePanelIds(StudyViewFilterContext studyViewFilterContext, String alterationType, List<String> hugoGeneSymbols) {
        return mapper.getMatchingGenePanelIds(createStudyViewFilterHelper(studyViewFilterContext), alterationType, hugoGeneSymbols)
            .stream()
            .collect(Collectors.groupingBy(GenePanelToGene::getHugoGeneSymbol,
                Collectors.mapping(GenePanelToGene::getGenePanelId, Collectors.toSet())));
    }

    @Override
    public int getTotalProfiledCountsByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType) {
       return mapper.getTotalProfiledCountByAlterationType(createStudyViewFilterHelper(studyViewFilterContext), alterationType); 
    }

    @Override
    public int getSampleProfileCountWithoutPanelData(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getSampleProfileCountWithoutPanelData(createStudyViewFilterHelper(studyViewFilterContext), alterationType);
    }


    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getClinicalEventTypeCounts(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<PatientTreatment> getPatientTreatments(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getPatientTreatments(createStudyViewFilterHelper(studyViewFilterContext));
    }
    
    @Override
    public int getTotalPatientTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
       return mapper.getPatientTreatmentCounts(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<SampleTreatment> getSampleTreatments(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getSampleTreatmentCounts(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public int getTotalSampleTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getTotalSampleTreatmentCounts(createStudyViewFilterHelper(studyViewFilterContext));
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
    
    @Override
    public List<GenomicDataCountItem> getCNACounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {

        return mapper.getCNACounts(createStudyViewFilterHelper(studyViewFilterContext), genomicDataFilters);
    }
    
    public List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {

        return mapper.getMutationCountsByType(createStudyViewFilterHelper(studyViewFilterContext), genomicDataFilters);
    }

}