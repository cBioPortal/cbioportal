package org.cbioportal.persistence.mybatiscolumnstore;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.webparam.CategorizedClinicalDataCountFilter;
import org.cbioportal.webparam.StudyViewFilter;

import java.util.List;

public interface StudyViewMapper {
    List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    
    List<ClinicalDataCount> getPatientClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                         boolean applyPatientIdFilters,  List<String> attributeIds, List<String> filterAttributeValues);

    List<ClinicalDataCount> getSampleClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                        boolean applyPatientIdFilters, List<String> attributeIds, List<String> filterAttributeValues );
    
    List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                  boolean applyPatientIdFilters, List<String> attributeIds, List<String> filterAttributeValues);
    
    List<String> getClinicalAttributeNames(String tableName);
}
