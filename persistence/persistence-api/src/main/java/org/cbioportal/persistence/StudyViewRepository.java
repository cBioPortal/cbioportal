package org.cbioportal.persistence;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.enums.ClinicalAttributeDataType;
import org.cbioportal.webparam.CategorizedClinicalDataCountFilter;
import org.cbioportal.webparam.StudyViewFilter;

import java.util.List;

public interface StudyViewRepository {
    List<Sample> getFilteredSamplesFromColumnstore(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);

    List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, List<String> attributeIds, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);
    
    List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, List<String> attributeIds, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);
    
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);
    
    List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes);
    
    List<ClinicalDataCount> getSampleClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes);
    
    List<ClinicalDataCount> getPatientClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes);

    List<String> getClinicalDataAttributeNames(ClinicalAttributeDataSource clinicalAttributeDataSource, ClinicalAttributeDataType dataType);

}
