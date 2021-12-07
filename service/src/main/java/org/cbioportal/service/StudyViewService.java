package org.cbioportal.service;

import org.cbioportal.model.*;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;
import java.util.Map;

public interface StudyViewService {
    List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds);

    List<AlterationCountByGene> getMutationAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;

    List<AlterationCountByGene> getStructuralVariantAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;

    List<CopyNumberCountByGene> getCNAAlterationCountByGenes(List<String> studyIds, List<String> sampleIds, AlterationFilter annotationFilter)
        throws StudyNotFoundException;

    List<GenericAssayDataCountItem> fetchGenericAssayDataCounts(List<String> sampleIds, List<String> studyIds, List<String> stableIds, List<String> profileTypes);
    
    Map<String, List<GenePanelCount>> fetchGenePanelCounts(List<String> sampleIds, List<String> studyIds);
}
