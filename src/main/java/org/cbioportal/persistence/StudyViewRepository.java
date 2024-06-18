package org.cbioportal.persistence;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.enums.ClinicalAttributeDataType;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;
import java.util.Map;

public interface StudyViewRepository {
    List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);

    List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);
    
    List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);
    
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);
    
    List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes);
    
    List<ClinicalDataCount> getSampleClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes);
    
    List<ClinicalDataCount> getPatientClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<String> filteredAttributes);
    
    List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);
    
    List<String> getClinicalDataAttributeNames(ClinicalAttributeDataSource clinicalAttributeDataSource, ClinicalAttributeDataType dataType);

    Map<String, AlterationCountByGene> getTotalProfiledCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, String alterationType);
    
    int getFilteredSamplesCount(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter);
    
    Map<String, AlterationCountByGene> getMatchingGenePanelIds(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, String alterationType);

    Map<String, Integer> getMutationCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, GenomicDataFilter genomicDataFilter);
    
    List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, List<GenomicDataFilter> genomicDataFilters);
}
