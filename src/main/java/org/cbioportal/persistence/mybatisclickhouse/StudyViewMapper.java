package org.cbioportal.persistence.mybatisclickhouse;

import org.apache.ibatis.annotations.MapKey;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;
import java.util.Map;


public interface StudyViewMapper {
    List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);

    List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                boolean applyPatientIdFilters, AlterationFilterHelper alterationFilterHelper);
    
    List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                            boolean applyPatientIdFilters, AlterationFilterHelper alterationFilterHelper);

    List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                boolean applyPatientIdFilters, AlterationFilterHelper alterationFilterHelper);
    
    List<ClinicalDataCount> getPatientClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                         boolean applyPatientIdFilters,  List<String> attributeIds, List<String> filteredAttributeValues);

    List<ClinicalDataCount> getSampleClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                        boolean applyPatientIdFilters, List<String> attributeIds, List<String> filteredAttributeValues );
    
    List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                  boolean applyPatientIdFilters, List<String> attributeIds, List<String> filteredAttributeValues);

    List<CaseListDataCount> getCaseListDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    
    List<ClinicalAttribute> getClinicalAttributes();
    
    List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<String> attributeIds);
    
    List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<String> attributeIds);
    
    List<String> getSampleCountWithoutClinicalData(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<String> attributeIds);
    
    @MapKey("hugoGeneSymbol")
    Map<String, AlterationCountByGene> getTotalProfiledCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, String alterationType);
    
    int getFilteredSamplesCount(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    
    List<GenePanelToGene> getMatchingGenePanelIds(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, String alterationType);
    
    int getTotalProfiledCountByAlterationType(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, String alterationType);
}
