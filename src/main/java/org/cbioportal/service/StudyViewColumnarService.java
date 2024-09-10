package org.cbioportal.service;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;
import java.util.Map;

public interface StudyViewColumnarService {

    List<Sample> getFilteredSamples(StudyViewFilter studyViewFilter);

    List<AlterationCountByGene> getMutatedGenes(StudyViewFilter interceptedStudyViewFilter);
    List<CopyNumberCountByGene> getCnaGenes(StudyViewFilter interceptedStudyViewFilter);
    List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilter studyViewFilter);

    Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap();
    
    List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes);

    List<CaseListDataCount> getCaseListDataCounts(StudyViewFilter studyViewFilter);

    List<ClinicalData> getPatientClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds);

    List<ClinicalData> getSampleClinicalData(StudyViewFilter studyViewFilter, List<String> attributeIds);

    List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilter studyViewFilter);

    List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilter studyViewFilter);
    PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter);
    SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter);

    List<GenomicDataCountItem> getCNACountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters);

    List<GenomicDataCountItem> getMutationCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters);

    List<ClinicalDataCountItem> getGenomicDataBinCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes);

    List<ClinicalDataCountItem> getGenericAssayDataBinCounts(StudyViewFilter studyViewFilter, List<String> filteredAttributes);
    
    List<GenomicDataCountItem> getMutationTypeCountsByGeneSpecific(StudyViewFilter studyViewFilter, List<GenomicDataFilter> genomicDataFilters);
}
