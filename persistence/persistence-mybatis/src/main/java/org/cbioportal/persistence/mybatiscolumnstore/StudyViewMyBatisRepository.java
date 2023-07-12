package org.cbioportal.persistence.mybatiscolumnstore;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.enums.ClinicalAttributeDataType;
import org.cbioportal.webparam.CategorizedClinicalDataCountFilter;
import org.cbioportal.webparam.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudyViewMyBatisRepository implements StudyViewRepository {

    private final List<String> FILTERED_CLINICAL_ATTR_VALUES = List.of("NA", "NAN", "N/A");
    @Autowired
    private StudyViewMapper studyViewMapper;
    
    @Override
    public List<Sample> getFilteredSamplesFromColumnstore(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        
        return studyViewMapper.getFilteredSamples(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter));
    }
    
    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return studyViewMapper.getMutatedGenes(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter));
    }

    @Override
    public List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes) {
        return studyViewMapper.getClinicalDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
            filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES );
    }

    @Override
    public List<ClinicalDataCount> getSampleClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes) {
        return studyViewMapper.getSampleClinicalDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
           filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES );
    }
    
    @Override
    public List<ClinicalDataCount> getPatientClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes) {
        return studyViewMapper.getPatientClinicalDataCounts(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter),
            filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES);
    }

    @Override
    public List<String> getClinicalDataAttributeNames(ClinicalAttributeDataSource clinicalAttributeDataSource, ClinicalAttributeDataType dataType) {
        String tableName = clinicalAttributeDataSource.getValue().toLowerCase() + "_clinical_attribute_" + dataType.getValue().toLowerCase();
        return studyViewMapper.getClinicalAttributeNames(tableName);
    }

    private boolean shouldApplyPatientIdFilters(CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters().isEmpty();
    }

    public List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, List<String> attributeIds, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return studyViewMapper.getSampleClinicalDataFromStudyViewFilter(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), attributeIds);
    }
    
    public List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, List<String> attributeIds, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter) {
        return studyViewMapper.getPatientClinicalDataFromStudyViewFilter(studyViewFilter, categorizedClinicalDataCountFilter, shouldApplyPatientIdFilters(categorizedClinicalDataCountFilter), attributeIds);
    }
}