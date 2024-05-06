package org.cbioportal.persistence.mybatiscolumnar;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;

public interface StudyViewMapper {
    List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);

    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    
    List<ClinicalDataCount> getPatientClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                         boolean applyPatientIdFilters,  List<String> attributeIds, List<String> filteredAttributeValues);

    List<ClinicalDataCount> getSampleClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                        boolean applyPatientIdFilters, List<String> attributeIds, List<String> filteredAttributeValues );
    
    List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                  boolean applyPatientIdFilters, List<String> attributeIds, List<String> filteredAttributeValues);
    
    List<String> getClinicalAttributeNames(String tableName);
    
    List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<String> attributeIds);
    
    List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<String> attributeIds);
    
}
