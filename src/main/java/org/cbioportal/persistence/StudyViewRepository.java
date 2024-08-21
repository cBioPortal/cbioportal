package org.cbioportal.persistence;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.PatientTreatment;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatment;
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.GenomicDataFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StudyViewRepository {
    List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext);
    
    List<ClinicalData> getSampleClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);
    
    List<ClinicalData> getPatientClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);
    
    List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext);
    
    List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext);
    List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext);
    
    List<ClinicalDataCount> getClinicalDataCounts(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);
    
    List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilterContext studyViewFilterContext);
    
    List<ClinicalAttribute> getClinicalAttributes();
    
    Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap();

    List<CaseListDataCount> getCaseListDataCountsPerStudy(StudyViewFilterContext studyViewFilterContext);

    Map<String, Integer> getTotalProfiledCounts(StudyViewFilterContext studyViewFilterContext, String alterationType);
    
    int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext);
    
    Map<String, Set<String>> getMatchingGenePanelIds(StudyViewFilterContext studyViewFilterContext, String alterationType);
    
    int getTotalProfiledCountsByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType);
    
    int getSampleProfileCountWithoutPanelData(StudyViewFilterContext studyViewFilterContext, String alterationType);
    
    List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilterContext studyViewFilterContext);
    
    List<PatientTreatment> getPatientTreatments(StudyViewFilterContext studyViewFilterContext);

    int getTotalPatientTreatmentCount(StudyViewFilterContext studyViewFilterContext);
    
    List<SampleTreatment> getSampleTreatments(StudyViewFilterContext studyViewFilterContext);

    int getTotalSampleTreatmentCount(StudyViewFilterContext studyViewFilterContext);

    List<GenomicDataCountItem> getCNACounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);

    Map<String, Integer> getMutationCounts(StudyViewFilterContext studyViewFilterContext, GenomicDataFilter genomicDataFilter);
    
    List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);
}
