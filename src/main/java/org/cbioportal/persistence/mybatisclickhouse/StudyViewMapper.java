package org.cbioportal.persistence.mybatisclickhouse;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.PatientTreatment;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatment;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.web.parameter.GenomicDataBinFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;

import java.util.List;
import java.util.Map;


public interface StudyViewMapper {
    List<Sample> getFilteredSamples(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);

    List<GenomicDataCount> getMolecularProfileSampleCounts(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);
    
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilterHelper studyViewFilterHelper, AlterationFilterHelper alterationFilterHelper);
    
    List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterHelper studyViewFilterHelper, AlterationFilterHelper alterationFilterHelper);

    List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterHelper studyViewFilterHelper, AlterationFilterHelper alterationFilterHelper);
    
    List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilterHelper studyViewFilterHelper, List<String> attributeIds, List<String> filteredAttributeValues);

    List<CaseListDataCount> getCaseListDataCountsPerStudy(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);
    
    List<ClinicalAttribute> getClinicalAttributes();
    
    List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(StudyViewFilterHelper studyViewFilterHelper, List<String> attributeIds);
    
    List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(StudyViewFilterHelper studyViewFilterHelper, List<String> attributeIds);
    
    List<AlterationCountByGene> getTotalProfiledCounts(StudyViewFilterHelper studyViewFilterHelper, String alterationType);
    
    int getFilteredSamplesCount(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);
    
    int getFilteredPatientsCount(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);
    
    List<GenePanelToGene> getMatchingGenePanelIds(StudyViewFilterHelper studyViewFilterHelper, String alterationType);
    
    int getTotalProfiledCountByAlterationType(StudyViewFilterHelper studyViewFilterHelper, String alterationType);
    
    int getSampleProfileCountWithoutPanelData(StudyViewFilterHelper studyViewFilterHelper, String alterationType);

    List<ClinicalEventTypeCount> getClinicalEventTypeCounts(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);
    
    List<PatientTreatment> getPatientTreatments(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);
    int getPatientTreatmentCounts(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);
    List<SampleTreatment> getSampleTreatmentCounts(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);
    int getTotalSampleTreatmentCounts(@Param("studyViewFilterHelper") StudyViewFilterHelper studyViewFilterHelper);

    List<GenomicDataCountItem> getCNACounts(StudyViewFilterHelper studyViewFilterHelper, List<GenomicDataFilter> genomicDataFilters);

    Map<String, Integer> getMutationCounts(StudyViewFilterHelper studyViewFilterHelper, GenomicDataFilter genomicDataFilter);

    List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterHelper studyViewFilterHelper, List<GenomicDataFilter> genomicDataFilters);
    
    List<ClinicalDataCount> getGenomicDataBinCounts(StudyViewFilterHelper studyViewFilterHelper, List<GenomicDataBinFilter> genomicDataBinFilters);
    
    List<ClinicalDataCount> getGenericAssayDataBinCounts(StudyViewFilterHelper studyViewFilterHelper, List<String> attributeIds);

    List<MolecularProfile> getGenericAssayProfiles();
}
