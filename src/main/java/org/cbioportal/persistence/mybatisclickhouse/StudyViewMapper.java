package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.PatientTreatment;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatment;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;


public interface StudyViewMapper {
    List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<CustomSampleIdentifier> customDataSamples);

    List<GenomicDataCount> getGenomicDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<CustomSampleIdentifier> customDataSamples);
    
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                boolean applyPatientIdFilters, AlterationFilterHelper alterationFilterHelper, List<CustomSampleIdentifier> customDataSamples);
    
    List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                            boolean applyPatientIdFilters, AlterationFilterHelper alterationFilterHelper, List<CustomSampleIdentifier> customDataSamples);

    List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                boolean applyPatientIdFilters, AlterationFilterHelper alterationFilterHelper, List<CustomSampleIdentifier> customDataSamples);
    
    List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter,
                                                  boolean applyPatientIdFilters, List<String> attributeIds, List<String> filteredAttributeValues, List<CustomSampleIdentifier> customDataSamples);

    List<CaseListDataCount> getCaseListDataCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<CustomSampleIdentifier> customDataSamples);
    
    List<ClinicalAttribute> getClinicalAttributes();
    
    List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<String> attributeIds, List<CustomSampleIdentifier> customDataSamples);
    
    List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, List<String> attributeIds, List<CustomSampleIdentifier> customDataSamples);
    
    List<AlterationCountByGene> getTotalProfiledCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, String alterationType, List<CustomSampleIdentifier> customDataSamples);
    
    int getFilteredSamplesCount(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    
    List<GenePanelToGene> getMatchingGenePanelIds(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, String alterationType, List<CustomSampleIdentifier> customDataSamples);
    
    int getTotalProfiledCountByAlterationType(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, String alterationType, List<CustomSampleIdentifier> customDataSamples);
    
    int getSampleProfileCountWithoutPanelData(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters, String alterationType, List<CustomSampleIdentifier> customDataSamples);

    List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    
    List<PatientTreatment> getPatientTreatments(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    int getPatientTreatmentCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    List<SampleTreatment> getSampleTreatmentCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
    int getTotalSampleTreatmentCounts(StudyViewFilter studyViewFilter, CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter, boolean applyPatientIdFilters);
}
