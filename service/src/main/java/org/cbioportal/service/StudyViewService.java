package org.cbioportal.service;

import org.cbioportal.model.*;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.webparam.StudyViewFilter;

import java.util.List;

public interface StudyViewService {
    List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds);

    List<AlterationCountByGene> getMutationAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;

    List<AlterationCountByGene> getStructuralVariantAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;

    List<CopyNumberCountByGene> getCNAAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;

    List<GenericAssayDataCountItem> fetchGenericAssayDataCounts(List<String> sampleIds, List<String> studyIds, List<String> stableIds, List<String> profileTypes);

    List<Sample> getFilteredSamplesFromColumnstore(StudyViewFilter studyViewFilter);

    List<AlterationCountByGene> getMutatedGenesFromColumnstore(StudyViewFilter interceptedStudyViewFilter);
    
    List<ClinicalDataCountItem> getClinicalDataCountsFromColumnStore(StudyViewFilter studyViewFilter, List<String> filteredAttributes);
    
    List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, List<String> attributeIds);
    
    List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(StudyViewFilter studyViewFilter, List<String> attributeIds);
}
