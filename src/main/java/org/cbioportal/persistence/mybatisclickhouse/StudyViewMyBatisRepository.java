package org.cbioportal.persistence.mybatisclickhouse;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.GenericAssayDataCountItem;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.PatientTreatment;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatment;
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.DataSource;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.service.util.StudyViewColumnarServiceUtil;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.GenomicDataBinFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class StudyViewMyBatisRepository implements StudyViewRepository {

    private final StudyViewMapper studyViewMapper;
    private Map<DataSource, List<ClinicalAttribute>> clinicalAttributesMap = new EnumMap<>(DataSource.class);
    private Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap = new EnumMap<>(DataSource.class);
    
    private static final List<String> FILTERED_CLINICAL_ATTR_VALUES = Collections.emptyList();
    private final StudyViewMapper mapper;
   
    @Autowired
    public StudyViewMyBatisRepository(StudyViewMapper mapper, StudyViewMapper studyViewMapper) {
        this.mapper = mapper;
        this.studyViewMapper = studyViewMapper;
    }
    
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredSamples(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<String> getFilteredStudyIds(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredStudyIds(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getMutatedGenes(createStudyViewFilterHelper(studyViewFilterContext),
            AlterationFilterHelper.build(studyViewFilterContext.studyViewFilter().getAlterationFilter()));
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
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
        return mapper.getClinicalDataCounts(createStudyViewFilterHelper(studyViewFilterContext),
            filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES);
    }

    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilterContext studyViewFilterContext) {
        var sampleCounts = mapper.getMolecularProfileSampleCounts(createStudyViewFilterHelper(studyViewFilterContext));
        return StudyViewColumnarServiceUtil.mergeGenomicDataCounts(sampleCounts);
        
    }
    
    public StudyViewFilterHelper createStudyViewFilterHelper(StudyViewFilterContext studyViewFilterContext) {
        return StudyViewFilterHelper.build(
            studyViewFilterContext.studyViewFilter(),
            getGenericAssayProfilesMap(),
            studyViewFilterContext.customDataFilterSamples(),
            studyViewFilterContext.involvedCancerStudies()
        );    
    }
    
    @Override
    public List<ClinicalAttribute> getClinicalAttributes() {
        return mapper.getClinicalAttributes();
    }

    @Override
    public List<MolecularProfile> getGenericAssayProfiles() {
        return mapper.getGenericAssayProfiles();
    }

    @Override
    public List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return studyViewMapper.getFilteredMolecularProfilesByAlterationType(createStudyViewFilterHelper(studyViewFilterContext), alterationType);
    }

    @Override
    public Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap() {
        if (clinicalAttributesMap.isEmpty()) {
            buildClinicalAttributeNameMap();
        }
        
        Map<String, ClinicalDataType> attributeDatatypeMap = new HashMap<>();

        clinicalAttributesMap
            .get(DataSource.SAMPLE)
            .forEach(attribute -> attributeDatatypeMap.put(attribute.getAttrId(), ClinicalDataType.SAMPLE));

        clinicalAttributesMap
            .get(DataSource.PATIENT)
            .forEach(attribute -> attributeDatatypeMap.put(attribute.getAttrId(), ClinicalDataType.PATIENT));
        
        return attributeDatatypeMap;
    }

    @Override
    public List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds) {
        return mapper.getClinicalAttributesForStudies(studyIds);
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
    public Map<String, Integer> getTotalProfiledCounts(StudyViewFilterContext studyViewFilterContext, String alterationType, List<MolecularProfile> molecularProfiles) {
        return mapper.getTotalProfiledCounts(createStudyViewFilterHelper(studyViewFilterContext), alterationType, molecularProfiles)
            .stream()
            .collect(Collectors.groupingBy(AlterationCountByGene::getHugoGeneSymbol,
                Collectors.mapping(AlterationCountByGene::getNumberOfProfiledCases, Collectors.summingInt(Integer::intValue))));
    }

    @Override
    public int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredSamplesCount(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public int getFilteredPatientCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredPatientsCount(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public Map<String, Set<String>> getMatchingGenePanelIds(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getMatchingGenePanelIds(createStudyViewFilterHelper(studyViewFilterContext), alterationType)
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
    
    @Override
    public List<ClinicalDataCount> getGenomicDataBinCounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataBinFilter> genomicDataBinFilters) {
        return mapper.getGenomicDataBinCounts(createStudyViewFilterHelper(studyViewFilterContext), genomicDataBinFilters);
    }

    @Override
    public List<ClinicalDataCount> getGenericAssayDataBinCounts(StudyViewFilterContext studyViewFilterContext, List<GenericAssayDataBinFilter> genericAssayDataBinFilters) {
        return mapper.getGenericAssayDataBinCounts(createStudyViewFilterHelper(studyViewFilterContext), genericAssayDataBinFilters);
    }
    
    private void buildClinicalAttributeNameMap() {
        clinicalAttributesMap = this.getClinicalAttributes()
            .stream()
            .collect(Collectors.groupingBy(ca -> ca.getPatientAttribute().booleanValue() ? DataSource.PATIENT : DataSource.SAMPLE));
    }

    private void buildGenericAssayProfilesMap() {
        genericAssayProfilesMap = this.getGenericAssayProfiles()
            .stream()
            .collect(Collectors.groupingBy(ca -> ca.getPatientLevel().booleanValue() ? DataSource.PATIENT : DataSource.SAMPLE));
    }

    private Map<DataSource, List<MolecularProfile>> getGenericAssayProfilesMap() {
        if (genericAssayProfilesMap.isEmpty()) {
            buildGenericAssayProfilesMap();
        }
        return genericAssayProfilesMap;
    }
    
    @Override
    public List<GenomicDataCountItem> getCNACounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
        return mapper.getCNACounts(createStudyViewFilterHelper(studyViewFilterContext), genomicDataFilters);
    }

    @Override
    public List<GenericAssayDataCountItem> getGenericAssayDataCounts(StudyViewFilterContext studyViewFilterContext, List<GenericAssayDataFilter> genericAssayDataFilters) {
        return mapper.getGenericAssayDataCounts(createStudyViewFilterHelper(studyViewFilterContext), genericAssayDataFilters);
    }

    public Map<String, Integer> getMutationCounts(StudyViewFilterContext studyViewFilterContext, GenomicDataFilter genomicDataFilter) {
        return mapper.getMutationCounts(createStudyViewFilterHelper(studyViewFilterContext), genomicDataFilter);
    }
    
    public List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
        return mapper.getMutationCountsByType(createStudyViewFilterHelper(studyViewFilterContext), genomicDataFilters);
    }

}