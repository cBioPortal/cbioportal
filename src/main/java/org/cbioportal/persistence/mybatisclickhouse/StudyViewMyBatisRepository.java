package org.cbioportal.persistence.mybatisclickhouse;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.enums.ClinicalAttributeDataType;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class StudyViewMyBatisRepository implements StudyViewRepository {

    private static final List<String> FILTERED_CLINICAL_ATTR_VALUES = Collections.emptyList();
    private final StudyViewMapper mapper;
   
    @Autowired
    public StudyViewMyBatisRepository(StudyViewMapper mapper) {
        this.mapper = mapper;    
    }
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        
        return mapper.getFilteredSamples(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter));
    }
    
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return mapper.getMutatedGenes(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
    }

    @Override
    public List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes) {
        return mapper.getClinicalDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
            filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES );
    }

    @Override
    public List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return mapper.getGenomicDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter));
    }

    @Override
    public List<ClinicalDataCount> getSampleClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes) {
        return mapper.getSampleClinicalDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
           filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES );
    }
    
    @Override
    public List<ClinicalDataCount> getPatientClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes) {
        return mapper.getPatientClinicalDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
            filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES);
    }

    @Override
    public List<String> getClinicalDataAttributeNames(ClinicalAttributeDataSource clinicalAttributeDataSource, ClinicalAttributeDataType dataType) {
        String tableName = clinicalAttributeDataSource.getValue().toLowerCase() + "_clinical_attribute_" + dataType.getValue().toLowerCase() + "_mv";
        return mapper.getClinicalAttributeNames(tableName);
    }

    private boolean shouldApplyPatientIdFilters(CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters().isEmpty();
    }

    public List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return mapper.getSampleClinicalDataFromStudyViewFilter(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), attributeIds);
    }
    
    public List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return mapper.getPatientClinicalDataFromStudyViewFilter(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), attributeIds);
    }
    
    public Map<String, AlterationCountByGene> getTotalProfiledCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, String alterationType) {
        return mapper.getTotalProfiledCounts(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), alterationType);
    }

    @Override
    public int getFilteredSamplesCount(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return mapper.getFilteredSamplesCount(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter));
    }

    @Override
    public Map<String, AlterationCountByGene> getMatchingGenePanelIds(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, String alterationType) {
        return mapper.getMatchingGenePanelIds(studyViewFilter, categorizedClinicalDataCountFilter,
            shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), alterationType);
    }
    
    public List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<GenomicDataFilter> genomicDataFilters) {
        return mapper.getMutationCountsByType(studyViewFilter, categorizedClinicalDataCountFilter, genomicDataFilters);
    }

}